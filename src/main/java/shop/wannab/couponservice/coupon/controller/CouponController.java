package shop.wannab.couponservice.coupon.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponsDto;
import shop.wannab.couponservice.coupon.dto.CouponResponseToUserDto;
import shop.wannab.couponservice.coupon.dto.CouponUsageRequestDto;
import shop.wannab.couponservice.coupon.dto.OrderCouponsRequestDto;
import shop.wannab.couponservice.coupon.dto.PageResponseDto;
import shop.wannab.couponservice.coupon.dto.TryApplyCouponsRequestDto;
import shop.wannab.couponservice.coupon.dto.TryApplyCouponsResponseDto;
import shop.wannab.couponservice.coupon.service.CouponService;
import shop.wannab.couponservice.couponpolicy.dto.IssuableCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.service.CouponPolicyService;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CouponPolicyService couponPolicyService;


    @PostMapping("/issue/welcome")
    public ResponseEntity<String> issueWelcomeCouponForNewUser(
            @RequestParam("userId") Long userId) {
        try {
            couponService.issueWelcomeCouponForNewUser(userId);
            return ResponseEntity.ok("웰컴 쿠폰이 성공적으로 발급되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/issue/birthday")
    public ResponseEntity<String> issueBirthdayCouponsManually(
            @RequestParam("month") int month) {
        try {
            couponService.issueBirthdayCoupon(month);
            return ResponseEntity.ok(month + "월 생일 쿠폰이 성공적으로 발급되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("생일 쿠폰 발급 실패: " + e.getMessage());
        }
    }

    @PostMapping("/issue/custom")
    public ResponseEntity<String> issueCustomCoupon(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("couponPolicyId") Long couponPolicyId){
        couponService.issueGeneralCoupon(userId, couponPolicyId);
        return ResponseEntity.ok("쿠폰이 성공적으로 발급되었습니다.");
    }

    @GetMapping("/issuable-coupons")
    public ResponseEntity<List<IssuableCouponPolicyDto>> getIssuableCoupons(
            @RequestParam Long bookId){
        List<IssuableCouponPolicyDto> couponList = couponPolicyService.findIssuablePoliciesForBook(bookId);
        return ResponseEntity.ok(couponList);
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponseDto<CouponResponseToUserDto>> getCouponsForUser(
            @RequestHeader("X-USER-ID") Long userId,
            Pageable pageable) {

        PageResponseDto<CouponResponseToUserDto> couponPage = couponService.getUserCoupons(userId, pageable);

        return ResponseEntity.ok(couponPage);
    }

    @PostMapping("/order")
    public ResponseEntity<ApplicableCouponsDto> getApplicableCoupons(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody OrderCouponsRequestDto orderCouponsRequestDtoList){
        return ResponseEntity.ok(couponService.getUserApplicableCoupons(userId, orderCouponsRequestDtoList));
    }

    @PostMapping("/order/apply")
    public ResponseEntity<List<TryApplyCouponsResponseDto>> getApplyCoupons(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody TryApplyCouponsRequestDto tryApplyCouponsRequestDtoMap
            ){
        return ResponseEntity.ok(couponService.applyCoupons(userId, tryApplyCouponsRequestDtoMap));
    }

    @PostMapping("/order/success")
    public ResponseEntity<Void> processUsedCoupons(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody CouponUsageRequestDto requestDto) {

        couponService.processUsedCoupons(userId, requestDto);
        return ResponseEntity.ok().build();
    }
}
