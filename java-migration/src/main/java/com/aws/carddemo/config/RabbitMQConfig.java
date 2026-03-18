package com.aws.carddemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String AUTH_REQUEST_QUEUE = "carddemo.auth.request";
    public static final String AUTH_REPLY_QUEUE = "carddemo.auth.reply";
    public static final String ACCOUNT_INQUIRY_QUEUE = "carddemo.account.inquiry";
    public static final String ACCOUNT_REPLY_QUEUE = "carddemo.account.reply";
    public static final String DATE_INQUIRY_QUEUE = "carddemo.date.inquiry";
    public static final String DATE_REPLY_QUEUE = "carddemo.date.reply";

    public static final String EXCHANGE = "carddemo.exchange";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue authRequestQueue() {
        return new Queue(AUTH_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue authReplyQueue() {
        return new Queue(AUTH_REPLY_QUEUE, true);
    }

    @Bean
    public Queue accountInquiryQueue() {
        return new Queue(ACCOUNT_INQUIRY_QUEUE, true);
    }

    @Bean
    public Queue accountReplyQueue() {
        return new Queue(ACCOUNT_REPLY_QUEUE, true);
    }

    @Bean
    public Queue dateInquiryQueue() {
        return new Queue(DATE_INQUIRY_QUEUE, true);
    }

    @Bean
    public Queue dateReplyQueue() {
        return new Queue(DATE_REPLY_QUEUE, true);
    }

    @Bean
    public Binding authRequestBinding(Queue authRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(authRequestQueue).to(exchange).with(AUTH_REQUEST_QUEUE);
    }

    @Bean
    public Binding authReplyBinding(Queue authReplyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(authReplyQueue).to(exchange).with(AUTH_REPLY_QUEUE);
    }

    @Bean
    public Binding accountInquiryBinding(Queue accountInquiryQueue, DirectExchange exchange) {
        return BindingBuilder.bind(accountInquiryQueue).to(exchange).with(ACCOUNT_INQUIRY_QUEUE);
    }

    @Bean
    public Binding accountReplyBinding(Queue accountReplyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(accountReplyQueue).to(exchange).with(ACCOUNT_REPLY_QUEUE);
    }

    @Bean
    public Binding dateInquiryBinding(Queue dateInquiryQueue, DirectExchange exchange) {
        return BindingBuilder.bind(dateInquiryQueue).to(exchange).with(DATE_INQUIRY_QUEUE);
    }

    @Bean
    public Binding dateReplyBinding(Queue dateReplyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(dateReplyQueue).to(exchange).with(DATE_REPLY_QUEUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
