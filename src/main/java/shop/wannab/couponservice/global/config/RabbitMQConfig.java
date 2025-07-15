package shop.wannab.couponservice.global.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final String WELCOME_COUPON_QUEUE_NAME = "wannab.welcome.coupon.queue";
    private static final String ORDER_CREATED_COUPON_QUEUE = "wannab.order.created.coupon.queue";

    @Bean
    public Queue welcomeQueue() {
        return new Queue(WELCOME_COUPON_QUEUE_NAME);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_COUPON_QUEUE);
    }
}
