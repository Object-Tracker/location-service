package com.example.location_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOCATION_EXCHANGE = "location.exchange";
    public static final String LOCATION_QUEUE = "location.queue";
    public static final String LOCATION_ROUTING_KEY = "location.update";

    @Bean
    public TopicExchange locationExchange() {
        return new TopicExchange(LOCATION_EXCHANGE);
    }

    @Bean
    public Queue locationQueue() {
        return new Queue(LOCATION_QUEUE, true);
    }

    @Bean
    public Binding locationBinding(Queue locationQueue, TopicExchange locationExchange) {
        return BindingBuilder.bind(locationQueue).to(locationExchange).with(LOCATION_ROUTING_KEY);
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
