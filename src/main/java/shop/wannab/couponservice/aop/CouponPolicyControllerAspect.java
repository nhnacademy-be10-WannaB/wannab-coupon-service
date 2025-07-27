package shop.wannab.couponservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@Aspect
public class CouponPolicyControllerAspect {
    @AfterReturning("execution(* shop.wannab.couponservice.couponpolicy.controller.CouponPolicyController.createCouponPolicy(..)) || " +
            "execution(* shop.wannab.couponservice.couponpolicy.controller.CouponPolicyController.deleteCouponPolicy(..))")
    public void logAdminActionCompletion(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        Long adminId = null;

        try {
            String adminIdHeader = request.getHeader("X-USER-ID");
            if (adminIdHeader != null && !adminIdHeader.isEmpty()) {
                adminId = Long.parseLong(adminIdHeader);
            }
        } catch (NumberFormatException e) {
            log.warn("action=logAdminActionCompletion, message=\"X-USER-ID 헤더 파싱 오류: {}\"", e.getMessage());
        }

        String methodName = joinPoint.getSignature().getName();
        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();

        String actionType = "";
        if (methodName.contains("create")) {
            actionType = "생성";
        } else if (methodName.contains("delete")) {
            actionType = "삭제";
        }

        log.info("action={}{}, endpoint={} ({}), adminId={}, message=\"쿠폰 정책 {} 성공.\"",
                methodName.substring(0, 1).toUpperCase() + methodName.substring(1),
                actionType,
                endpoint, httpMethod, adminId != null ? adminId : "N/A", actionType);
    }

    @AfterReturning("execution(* shop.wannab.couponservice.couponpolicy.controller.CouponPolicyController.getAllCouponPolicies(..))")
    public void logGetAllCouponPoliciesCompletion(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        String methodName = joinPoint.getSignature().getName();
        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();

        log.info("action={}, endpoint={} ({}), message=\"모든 쿠폰 정책 조회 성공.\"",
                methodName, endpoint, httpMethod);
    }
}
