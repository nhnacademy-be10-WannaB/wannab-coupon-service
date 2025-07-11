package shop.wannab.couponservice.global.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import shop.wannab.couponservice.coupon.service.CouponService;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventListener {
    private final CouponService couponService;

    @RabbitListener(queues = "wannab.welcome.coupon.queue")
    public void handleUserSignedUpEvent(Long userId){
        log.info("회원가입 이벤트 수신 유저 ID: {}", userId);
        try{
            couponService.issueWelcomeCouponForNewUser(userId);
            log.info("유저 ID {}에게 웰컴 쿠폰 발급 성공", userId);
        } catch(Exception e){
            log.error("웰컴 쿠폰 발급 실패 유저 ID: {}", userId, e);
        }
    }
}
