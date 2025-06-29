package shop.wannab.couponservice.controller;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.couponservice.domain.coupon.dto.ApplicableCouponsDto;
import shop.wannab.couponservice.domain.coupon.dto.CouponResponseToUserDto;
import shop.wannab.couponservice.domain.coupon.dto.CouponUsageRequestDto;
import shop.wannab.couponservice.domain.coupon.dto.OrderCouponsRequestDto;
import shop.wannab.couponservice.domain.coupon.dto.PageResponseDto;
import shop.wannab.couponservice.domain.coupon.dto.TryApplyCouponsRequestDto;
import shop.wannab.couponservice.domain.coupon.dto.TryApplyCouponsResponseDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.IssuableCouponPolicyDto;
import shop.wannab.couponservice.service.CouponPolicyService;
import shop.wannab.couponservice.service.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService couponService;
    private final CouponPolicyService couponPolicyService;

    public CouponController(CouponService couponService, CouponPolicyService couponPolicyService) {
        this.couponService = couponService;
        this.couponPolicyService = couponPolicyService;
    }

    @PostMapping("/issue/welcome")
    public ResponseEntity<String> issueWelcomeCouponForNewUser(
            @RequestHeader("X-User-Id") Long userId) {
        try {
            couponService.issueWelcomeCouponForNewUser(userId);
            return ResponseEntity.ok("웰컴 쿠폰이 성공적으로 발급되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/issue/custom")
    public ResponseEntity<String> issueCustomCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long couponPolicyId){
        couponService.issueGeneralCoupon(userId, couponPolicyId);
        return ResponseEntity.ok("쿠폰이 성공적으로 발급되었습니다.");
    }

    @GetMapping("/issuable-coupons")
    public ResponseEntity<List<IssuableCouponPolicyDto>> getIssuableCoupons(
            @RequestParam Long bookId,
            @RequestParam Long categoryId){
        List<IssuableCouponPolicyDto> couponList = couponPolicyService.findIssuablePoliciesForBook(bookId,categoryId);
        return ResponseEntity.ok(couponList);
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponseDto<CouponResponseToUserDto>> getCouponsForUser(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable) {

        // 자신의 서비스 로직을 호출하여 결과를 가져옵니다.
        PageResponseDto<CouponResponseToUserDto> couponPage = couponService.getUserCoupons(userId, pageable);

        // 결과를 HTTP 응답 바디에 담아 반환합니다.
        return ResponseEntity.ok(couponPage);
    }

    @PostMapping("/order")
    public ResponseEntity<ApplicableCouponsDto> getApplicableCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody OrderCouponsRequestDto orderCouponsRequestDtoList){
        return ResponseEntity.ok(couponService.getUserApplicableCoupons(userId, orderCouponsRequestDtoList));
    }

    @PostMapping("/order/apply")
    public ResponseEntity<List<TryApplyCouponsResponseDto>> getApplyCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody TryApplyCouponsRequestDto tryApplyCouponsRequestDtoMap
            ){
        return ResponseEntity.ok(couponService.applyCoupons(userId, tryApplyCouponsRequestDtoMap));
    }

    @PostMapping("/order/success")
    public ResponseEntity<Void> processUsedCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CouponUsageRequestDto requestDto) {

        couponService.processUsedCoupons(userId, requestDto);
        return ResponseEntity.ok().build();
    }
}
