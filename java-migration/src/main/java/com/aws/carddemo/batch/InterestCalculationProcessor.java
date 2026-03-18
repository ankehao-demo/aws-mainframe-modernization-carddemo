package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.DisclosureGroup;
import com.aws.carddemo.entity.TransactionCategoryBalance;
import com.aws.carddemo.repository.DisclosureGroupRepository;
import com.aws.carddemo.repository.TransactionCategoryBalanceRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InterestCalculationProcessor implements ItemProcessor<Account, Account> {

    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionCategoryBalanceRepository tranCatBalRepository;

    public InterestCalculationProcessor(DisclosureGroupRepository disclosureGroupRepository,
                                        TransactionCategoryBalanceRepository tranCatBalRepository) {
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.tranCatBalRepository = tranCatBalRepository;
    }

    @Override
    public Account process(Account account) throws Exception {
        if (!"Y".equals(account.getAcctActiveStatus())) {
            return null; // Skip inactive accounts
        }

        String groupId = account.getAcctGroupId();
        if (groupId == null || groupId.isBlank()) {
            return null; // Skip accounts without disclosure group
        }

        // Look up disclosure groups for this account's group
        List<DisclosureGroup> disclosureGroups = disclosureGroupRepository
                .findByDisAcctGroupId(groupId.trim());

        if (disclosureGroups.isEmpty()) {
            return null;
        }

        // Look up tran_cat_balance records for this account
        List<TransactionCategoryBalance> balances = tranCatBalRepository
                .findByTrancatAcctId(account.getAcctId());

        BigDecimal totalInterest = BigDecimal.ZERO;

        // Calculate interest for each category balance
        for (TransactionCategoryBalance balance : balances) {
            // Find matching disclosure group for this type/category
            DisclosureGroup matchingGroup = disclosureGroups.stream()
                    .filter(dg -> dg.getDisTranTypeCd().trim().equals(balance.getTrancatTypeCd().trim())
                            && dg.getDisTranCatCd().equals(balance.getTrancatCd()))
                    .findFirst()
                    .orElse(null);

            if (matchingGroup != null && matchingGroup.getDisIntRate().compareTo(BigDecimal.ZERO) > 0) {
                // Monthly interest = balance * (annual_rate / 12 / 100)
                BigDecimal monthlyRate = matchingGroup.getDisIntRate()
                        .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
                BigDecimal interest = balance.getTranCatBal().multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                totalInterest = totalInterest.add(interest);
            }
        }

        // Update account: curr_bal += total_interest, reset cycle credits/debits
        account.setAcctCurrBal(account.getAcctCurrBal().add(totalInterest));
        account.setAcctCurrCycCredit(BigDecimal.ZERO);
        account.setAcctCurrCycDebit(BigDecimal.ZERO);

        return account;
    }
}
