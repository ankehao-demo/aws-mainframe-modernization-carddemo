package com.carddemo.migration.runner;

import com.carddemo.migration.service.DataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command-line runner that performs the data migration.
 * Reads EBCDIC files from the configured data directory and loads into PostgreSQL.
 * 
 * Usage: java -jar data-migration.jar --data.dir=/path/to/app/data/EBCDIC
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationRunner implements CommandLineRunner {

    private final DataMigrationService migrationService;

    @Override
    public void run(String... args) throws Exception {
        String dataDir = "../../app/data/EBCDIC";
        for (String arg : args) {
            if (arg.startsWith("--data.dir=")) {
                dataDir = arg.substring("--data.dir=".length());
            }
        }

        Path basePath = Paths.get(dataDir);
        if (!Files.exists(basePath)) {
            log.error("Data directory not found: {}", basePath.toAbsolutePath());
            log.info("Usage: java -jar data-migration.jar --data.dir=/path/to/EBCDIC/data");
            return;
        }

        log.info("Starting data migration from: {}", basePath.toAbsolutePath());
        long startTime = System.currentTimeMillis();

        migrateIfExists(basePath, "AWS00011.CARDDEMO.USRSEC.PS", "users",
                path -> migrationService.migrateUsers(path));
        migrateIfExists(basePath, "AWS00011.CARDDEMO.CUSTDATA.PS", "customers",
                path -> migrationService.migrateCustomers(path));
        migrateIfExists(basePath, "AWS00011.CARDDEMO.ACCTDATA.PS", "accounts",
                path -> migrationService.migrateAccounts(path));
        migrateIfExists(basePath, "AWS00011.CARDDEMO.CARDDATA.PS", "cards",
                path -> migrationService.migrateCards(path));
        migrateIfExists(basePath, "AWS00011.CARDDEMO.TRANSACT.PS", "transactions",
                path -> migrationService.migrateTransactions(path));

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Data migration completed in {} ms", elapsed);
    }

    private void migrateIfExists(Path basePath, String fileName, String description,
                                  MigrationTask task) {
        Path filePath = basePath.resolve(fileName);
        if (Files.exists(filePath)) {
            try {
                log.info("Migrating {} from {}", description, fileName);
                task.execute(filePath);
            } catch (Exception e) {
                log.error("Failed to migrate {}: {}", description, e.getMessage(), e);
            }
        } else {
            log.warn("File not found, skipping {}: {}", description, filePath);
        }
    }

    @FunctionalInterface
    interface MigrationTask {
        void execute(Path path) throws Exception;
    }
}
