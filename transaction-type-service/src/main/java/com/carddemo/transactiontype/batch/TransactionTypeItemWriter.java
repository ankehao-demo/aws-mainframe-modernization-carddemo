package com.carddemo.transactiontype.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.carddemo.transactiontype.service.TransactionTypeService;

/**
 * Spring Batch ItemWriter that dispatches batch records to the service layer.
 *
 * Replaces the mainline processing logic in COBTUPDT.cbl (MNTTRDB2 batch job):
 * - Record type 'A' (Add): calls service.upsert()
 * - Record type 'U' (Update): calls service.upsert()
 * - Record type 'D' (Delete): calls service.delete()
 * - Record type '*' (Comment): skipped
 */
@Component
public class TransactionTypeItemWriter implements ItemWriter<TrRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionTypeItemWriter.class);

    private final TransactionTypeService service;

    public TransactionTypeItemWriter(TransactionTypeService service) {
        this.service = service;
    }

    @Override
    public void write(Chunk<? extends TrRecord> chunk) throws Exception {
        for (TrRecord record : chunk) {
            String type = record.getRecordType();
            if (type == null || type.isEmpty()) {
                log.warn("Skipping record with empty record type");
                continue;
            }

            switch (type.toUpperCase()) {
                case "A":
                case "U":
                    log.info("Upserting transaction type: {} - {}",
                            record.getTrType(), record.getTrDescription());
                    service.upsert(
                            record.getTrType().trim(),
                            record.getTrDescription() != null
                                    ? record.getTrDescription().trim() : "");
                    break;
                case "D":
                    log.info("Deleting transaction type: {}", record.getTrType());
                    service.delete(record.getTrType().trim());
                    break;
                case "*":
                    log.debug("Skipping comment record");
                    break;
                default:
                    log.warn("Unknown record type '{}', skipping", type);
                    break;
            }
        }
    }
}
