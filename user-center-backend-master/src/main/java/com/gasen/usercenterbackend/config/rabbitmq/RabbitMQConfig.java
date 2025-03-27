package com.gasen.usercenterbackend.config.rabbitmq;

import lombok.Getter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Configuration
public class RabbitMQConfig {

    @Value("${like.exchange}")
    private String likeExchange;

    @Value("${like.queue}")
    private String likeQueue;

    @Value("${like.routingKey}")
    private String likeRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(likeExchange);
    }

    @Bean
    public Queue queue() {
        return new Queue(likeQueue, true);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(likeRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 投诉消息队列
     */
    @Bean
    public Queue complaintQueue() {
        // durable=true 表示队列持久化
        return new Queue("complaint.queue", true);
    }
}
