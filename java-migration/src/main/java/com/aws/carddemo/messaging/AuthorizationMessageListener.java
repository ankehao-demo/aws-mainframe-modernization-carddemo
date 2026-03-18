package com.aws.carddemo.messaging;

import com.aws.carddemo.config.RabbitMQConfig;
import com.aws.carddemo.dto.AuthorizationRequest;
import com.aws.carddemo.dto.AuthorizationResponse;
import com.aws.carddemo.service.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMessageListener.class);

    private final AuthorizationService authorizationService;
    private final RabbitTemplate rabbitTemplate;

    public AuthorizationMessageListener(AuthorizationService authorizationService,
                                        RabbitTemplate rabbitTemplate) {
        this.authorizationService = authorizationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_REQUEST_QUEUE)
    public void handleAuthorizationRequest(AuthorizationRequest request) {
        logger.info("Received authorization request for card: {}", request.getCardNum());

        try {
            AuthorizationResponse response = authorizationService.processAuthorization(request);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.AUTH_REPLY_QUEUE, response);
            logger.info("Authorization response sent: {} - {}", response.getResponseCode(), response.getMessage());
        } catch (Exception e) {
            logger.error("Error processing authorization request: {}", e.getMessage(), e);
            AuthorizationResponse errorResponse = AuthorizationResponse.builder()
                    .cardNum(request.getCardNum())
                    .responseCode("9999")
                    .message("Error processing authorization: " + e.getMessage())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.AUTH_REPLY_QUEUE, errorResponse);
        }
    }
}
