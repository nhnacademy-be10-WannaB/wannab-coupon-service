package shop.wannab.couponservice.global.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import shop.wannab.couponservice.coupon.dto.CouponUsageRequestDto;
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

    @RabbitListener(queues = "wannab.order.created.coupon.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCreatedEvent(CouponUsageRequestDto requestDto){
        long userId = requestDto.getUserId();
        log.info("주문 적용 쿠폰 이벤트 수신, 유저 ID: {}",userId);
        try{
            couponService.processUsedCoupons(userId, requestDto);
            log.info("쿠폰 적용 완료, 유저 ID: {}", userId);
        }catch (Exception e){
            log.error("쿠폰 적용 실패, 유저 ID: {}",userId,e);
        }
    }
}
