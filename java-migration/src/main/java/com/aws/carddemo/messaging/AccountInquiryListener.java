package com.aws.carddemo.messaging;

import com.aws.carddemo.config.RabbitMQConfig;
import com.aws.carddemo.dto.AccountDto;
import com.aws.carddemo.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountInquiryListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountInquiryListener.class);

    private final AccountService accountService;
    private final RabbitTemplate rabbitTemplate;

    public AccountInquiryListener(AccountService accountService, RabbitTemplate rabbitTemplate) {
        this.accountService = accountService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_INQUIRY_QUEUE)
    public void handleAccountInquiry(Map<String, String> inquiry) {
        String acctId = inquiry.get("acctId");
        logger.info("Received account inquiry for: {}", acctId);

        try {
            AccountDto account = accountService.getAccount(acctId);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ACCOUNT_REPLY_QUEUE, account);
            logger.info("Account inquiry response sent for: {}", acctId);
        } catch (Exception e) {
            logger.error("Error processing account inquiry: {}", e.getMessage(), e);
            Map<String, String> errorResponse = Map.of(
                    "error", "Account not found: " + acctId,
                    "acctId", acctId != null ? acctId : "");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ACCOUNT_REPLY_QUEUE, errorResponse);
        }
    }
}
