package shop.wannab.couponservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class CouponIssueLogAspect {

    @Pointcut("execution(public * shop.wannab.couponservice.coupon.controller.CouponController.issueWelcomeCouponForNewUser(..)) || " +
            "execution(public * shop.wannab.couponservice.coupon.controller.CouponController.issueBirthdayCouponsManually(..)) || " +
            "execution(public * shop.wannab.couponservice.coupon.controller.CouponController.issueCustomCoupon(..))")
    public void couponIssueMethods() {}

    @AfterReturning(pointcut = "execution(* shop.wannab.couponservice.coupon.controller.CouponController.issueWelcomeCouponForNewUser(Long)) && args(userId)", argNames = "joinPoint,userId")
    public void logIssueWelcomeCouponSuccess(JoinPoint joinPoint, Long userId) {
        log.info("action={}, userId={}, message=\"웰컴 쿠폰 발급 성공.\"", joinPoint.getSignature().getName(), userId);
    }

    @AfterReturning(pointcut = "execution(* shop.wannab.couponservice.coupon.controller.CouponController.issueBirthdayCouponsManually(int)) && args(month)", argNames = "joinPoint,month")
    public void logIssueBirthdayCouponsSuccess(JoinPoint joinPoint, int month) {
        log.info("action={}, month={}, message=\"생일 쿠폰 발급 성공.\"", joinPoint.getSignature().getName(), month);
    }

    @AfterReturning(pointcut = "execution(* shop.wannab.couponservice.coupon.controller.CouponController.issueCustomCoupon(Long, Long)) && args(userId, couponPolicyId)", argNames = "joinPoint,userId,couponPolicyId")
    public void logIssueCustomCouponSuccess(JoinPoint joinPoint, Long userId, Long couponPolicyId) {
        log.info("action={}, userId={}, couponPolicyId={}, message=\"커스텀 쿠폰 발급 성공.\"", joinPoint.getSignature().getName(), userId, couponPolicyId);
    }
}
