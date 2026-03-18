package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatementGenerationProcessor implements ItemProcessor<Account, Account> {

    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final String outputDirectory;

    public StatementGenerationProcessor(CardXrefRepository cardXrefRepository,
                                        CustomerRepository customerRepository,
                                        TransactionRepository transactionRepository,
                                        @Value("${app.batch.output-dir}") String outputDirectory) {
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Account process(Account account) throws Exception {
        if (!"Y".equals(account.getAcctActiveStatus())) {
            return null;
        }

        // Find customer via card_xref
        Customer customer = cardXrefRepository.findFirstByXrefAcctId(account.getAcctId())
                .flatMap(xref -> customerRepository.findById(xref.getXrefCustId()))
                .orElse(null);

        if (customer == null) {
            return null;
        }

        // Get transactions for this account's cards
        List<Transaction> transactions = cardXrefRepository.findByXrefAcctId(account.getAcctId()).stream()
                .flatMap(xref -> transactionRepository.findByTranCardNum(
                        xref.getXrefCardNum(), PageRequest.of(0, 1000)).getContent().stream())
                .toList();

        // Create output directory
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String acctId = account.getAcctId().trim();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Generate text statement
        generateTextStatement(account, customer, transactions, outputDir, acctId, dateStr);

        // Generate HTML statement
        generateHtmlStatement(account, customer, transactions, outputDir, acctId, dateStr);

        return account;
    }

    private void generateTextStatement(Account account, Customer customer,
                                       List<Transaction> transactions, File outputDir,
                                       String acctId, String dateStr) throws IOException {
        File textFile = new File(outputDir, "statement_" + acctId + "_" + dateStr + ".txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(textFile))) {
            pw.println("=".repeat(60));
            pw.println("CREDIT CARD STATEMENT");
            pw.println("=".repeat(60));
            pw.printf("Account: %s%n", acctId);
            pw.printf("Customer: %s %s %s%n",
                    customer.getCustFirstName().trim(),
                    customer.getCustMiddleName().trim(),
                    customer.getCustLastName().trim());
            pw.printf("Statement Date: %s%n", LocalDate.now());
            pw.println("-".repeat(60));
            pw.printf("Current Balance: $%,.2f%n", account.getAcctCurrBal());
            pw.printf("Credit Limit: $%,.2f%n", account.getAcctCreditLimit());
            pw.printf("Available Credit: $%,.2f%n",
                    account.getAcctCreditLimit().subtract(account.getAcctCurrBal()));
            pw.println("-".repeat(60));
            pw.println("TRANSACTIONS");
            pw.println("-".repeat(60));
            pw.printf("%-16s %-30s %12s%n", "TRAN ID", "DESCRIPTION", "AMOUNT");
            pw.println("-".repeat(60));

            BigDecimal total = BigDecimal.ZERO;
            for (Transaction tran : transactions) {
                pw.printf("%-16s %-30s %12.2f%n",
                        tran.getTranId().trim(),
                        tran.getTranDesc() != null ? tran.getTranDesc().trim() : "",
                        tran.getTranAmt());
                total = total.add(tran.getTranAmt());
            }

            pw.println("-".repeat(60));
            pw.printf("%-46s %12.2f%n", "TOTAL:", total);
            pw.println("=".repeat(60));
        }
    }

    private void generateHtmlStatement(Account account, Customer customer,
                                       List<Transaction> transactions, File outputDir,
                                       String acctId, String dateStr) throws IOException {
        File htmlFile = new File(outputDir, "statement_" + acctId + "_" + dateStr + ".html");
        try (PrintWriter pw = new PrintWriter(new FileWriter(htmlFile))) {
            pw.println("<!DOCTYPE html>");
            pw.println("<html><head><title>Credit Card Statement</title>");
            pw.println("<style>body{font-family:Arial,sans-serif;margin:20px}");
            pw.println("table{border-collapse:collapse;width:100%}");
            pw.println("th,td{border:1px solid #ddd;padding:8px;text-align:left}");
            pw.println("th{background-color:#4CAF50;color:white}");
            pw.println(".total{font-weight:bold;background-color:#f2f2f2}</style></head>");
            pw.println("<body>");
            pw.println("<h1>Credit Card Statement</h1>");
            pw.printf("<p><strong>Account:</strong> %s</p>%n", acctId);
            pw.printf("<p><strong>Customer:</strong> %s %s %s</p>%n",
                    customer.getCustFirstName().trim(),
                    customer.getCustMiddleName().trim(),
                    customer.getCustLastName().trim());
            pw.printf("<p><strong>Statement Date:</strong> %s</p>%n", LocalDate.now());
            pw.println("<h2>Account Summary</h2>");
            pw.println("<table>");
            pw.printf("<tr><td>Current Balance</td><td>$%,.2f</td></tr>%n", account.getAcctCurrBal());
            pw.printf("<tr><td>Credit Limit</td><td>$%,.2f</td></tr>%n", account.getAcctCreditLimit());
            pw.printf("<tr><td>Available Credit</td><td>$%,.2f</td></tr>%n",
                    account.getAcctCreditLimit().subtract(account.getAcctCurrBal()));
            pw.println("</table>");
            pw.println("<h2>Transactions</h2>");
            pw.println("<table><tr><th>Tran ID</th><th>Description</th><th>Amount</th></tr>");

            BigDecimal total = BigDecimal.ZERO;
            for (Transaction tran : transactions) {
                pw.printf("<tr><td>%s</td><td>%s</td><td>$%,.2f</td></tr>%n",
                        tran.getTranId().trim(),
                        tran.getTranDesc() != null ? tran.getTranDesc().trim() : "",
                        tran.getTranAmt());
                total = total.add(tran.getTranAmt());
            }

            pw.printf("<tr class='total'><td colspan='2'>Total</td><td>$%,.2f</td></tr>%n", total);
            pw.println("</table>");
            pw.println("</body></html>");
        }
    }
}
