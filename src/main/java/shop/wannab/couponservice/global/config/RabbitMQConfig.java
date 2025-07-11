package shop.wannab.couponservice.global.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final String QUEUE_NAME = "wannab.welcome.coupon.queue";

    @Bean
    public Queue welcomeQueue() {
        return new Queue(QUEUE_NAME);
    }
}
