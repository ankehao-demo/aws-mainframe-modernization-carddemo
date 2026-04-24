package com.carddemo.cardservice.migration;

import com.carddemo.cardservice.model.Card;
import com.carddemo.cardservice.model.CardXref;
import com.carddemo.cardservice.repository.CardRepository;
import com.carddemo.cardservice.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data migration utility that reads EBCDIC fixed-length records from the
 * mainframe sample data files and inserts them into PostgreSQL.
 *
 * Source files:
 * - CARDDATA: 150-byte records using copybook CVACT02Y
 * - CARDXREF: 50-byte records using copybook CVACT03Y
 *
 * Activated only with the "migrate" Spring profile:
 *   java -jar card-service.jar --spring.profiles.active=migrate \
 *        --migration.carddata.path=/path/to/CARDDATA.PS \
 *        --migration.cardxref.path=/path/to/CARDXREF.PS
 */
@Component
@Profile("migrate")
public class EbcdicDataMigrator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EbcdicDataMigrator.class);

    private static final int CARD_RECORD_LENGTH = 150;
    private static final int XREF_RECORD_LENGTH = 50;
    private static final Charset EBCDIC_CHARSET = Charset.forName("IBM037");

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final MigrationProperties migrationProperties;

    public EbcdicDataMigrator(CardRepository cardRepository,
                              CardXrefRepository cardXrefRepository,
                              MigrationProperties migrationProperties) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.migrationProperties = migrationProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        if (migrationProperties.getCarddataPath() != null) {
            migrateCardData(Path.of(migrationProperties.getCarddataPath()));
        }
        if (migrationProperties.getCardxrefPath() != null) {
            migrateCardXrefData(Path.of(migrationProperties.getCardxrefPath()));
        }
    }

    /**
     * Reads 150-byte EBCDIC records matching CVACT02Y copybook layout:
     * - Bytes 0-15:  CARD-NUM           PIC X(16)
     * - Bytes 16-26: CARD-ACCT-ID       PIC 9(11)
     * - Bytes 27-29: CARD-CVV-CD        PIC 9(03)
     * - Bytes 30-79: CARD-EMBOSSED-NAME PIC X(50)
     * - Bytes 80-89: CARD-EXPIRATION-DATE PIC X(10)
     * - Byte  90:    CARD-ACTIVE-STATUS PIC X(01)
     * - Bytes 91-149: FILLER
     */
    public void migrateCardData(Path filePath) throws IOException {
        log.info("Migrating card data from: {}", filePath);
        int count = 0;

        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[CARD_RECORD_LENGTH];
            int bytesRead;

            while ((bytesRead = readFully(is, buffer)) == CARD_RECORD_LENGTH) {
                String record = new String(buffer, EBCDIC_CHARSET);

                String cardNum = record.substring(0, 16).trim();
                String cardAcctId = record.substring(16, 27).trim();
                String cardCvvCd = record.substring(27, 30).trim();
                String cardEmbossedName = record.substring(30, 80).trim();
                String cardExpirationDate = record.substring(80, 90).trim();
                String cardActiveStatus = record.substring(90, 91).trim();

                if (!cardNum.isBlank()) {
                    Card card = new Card(cardNum, cardAcctId, cardCvvCd,
                            cardEmbossedName, cardExpirationDate, cardActiveStatus);
                    cardRepository.save(card);
                    count++;
                }
            }
        }

        log.info("Migrated {} card records", count);
    }

    /**
     * Reads 50-byte EBCDIC records matching CVACT03Y copybook layout:
     * - Bytes 0-15:  XREF-CARD-NUM PIC X(16)
     * - Bytes 16-24: XREF-CUST-ID  PIC 9(09)
     * - Bytes 25-35: XREF-ACCT-ID  PIC 9(11)
     * - Bytes 36-49: FILLER
     */
    public void migrateCardXrefData(Path filePath) throws IOException {
        log.info("Migrating card xref data from: {}", filePath);
        int count = 0;

        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[XREF_RECORD_LENGTH];
            int bytesRead;

            while ((bytesRead = readFully(is, buffer)) == XREF_RECORD_LENGTH) {
                String record = new String(buffer, EBCDIC_CHARSET);

                String xrefCardNum = record.substring(0, 16).trim();
                String xrefCustId = record.substring(16, 25).trim();
                String xrefAcctId = record.substring(25, 36).trim();

                if (!xrefCardNum.isBlank()) {
                    CardXref xref = new CardXref(xrefCardNum, xrefCustId, xrefAcctId);
                    cardXrefRepository.save(xref);
                    count++;
                }
            }
        }

        log.info("Migrated {} card xref records", count);
    }

    private int readFully(InputStream is, byte[] buffer) throws IOException {
        int offset = 0;
        int remaining = buffer.length;
        while (remaining > 0) {
            int read = is.read(buffer, offset, remaining);
            if (read == -1) {
                return offset > 0 ? offset : -1;
            }
            offset += read;
            remaining -= read;
        }
        return offset;
    }
}
