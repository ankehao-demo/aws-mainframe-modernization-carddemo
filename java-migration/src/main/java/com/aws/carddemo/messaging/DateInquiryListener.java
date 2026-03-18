package com.aws.carddemo.messaging;

import com.aws.carddemo.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DateInquiryListener {

    private static final Logger logger = LoggerFactory.getLogger(DateInquiryListener.class);

    private final RabbitTemplate rabbitTemplate;

    public DateInquiryListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.DATE_INQUIRY_QUEUE)
    public void handleDateInquiry(Map<String, String> inquiry) {
        logger.info("Received date inquiry");

        Map<String, String> response = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        response.put("date", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        response.put("time", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        response.put("dayOfWeek", LocalDate.now().getDayOfWeek().toString());
        response.put("timestamp", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")));

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.DATE_REPLY_QUEUE, response);
        logger.info("Date inquiry response sent");
    }
}
