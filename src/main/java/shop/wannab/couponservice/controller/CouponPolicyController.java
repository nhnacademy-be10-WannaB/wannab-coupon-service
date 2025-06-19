package shop.wannab.couponservice.controller;


import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.couponservice.domain.couponpolicy.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.UpdateCouponPolicyDto;
import shop.wannab.couponservice.service.CouponPolicyService;


@RestController
@RequestMapping("/api/admin/coupon_policies")
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;

    public CouponPolicyController(CouponPolicyService couponPolicyService) {
        this.couponPolicyService = couponPolicyService;
    }

    @PostMapping
    public ResponseEntity<Void> createCouponPolicy(@Valid @RequestBody CreateCouponPolicyDto createCouponPolicyDto) {
        couponPolicyService.createCouponPolicy(createCouponPolicyDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CouponPolicyResponseDto>> getAllCouponPolicies() {
        List<CouponPolicyResponseDto> policies = couponPolicyService.getCouponPolicies();
        return ResponseEntity.ok(policies);
    }

    //쿠폰 정책 상세 조회
//    @GetMapping("/{policyId}")
//    public ResponseEntity<CouponPolicyDetailResponseDto> getCouponPolicyDetail(@PathVariable Long policyId) {
//        CouponPolicyDetailResponseDto respDto = couponPolicyService.getCouponPolicyById(policyId);
//        return ResponseEntity.ok(respDto);
//    }

    @PutMapping("/{policyId}")
    public ResponseEntity<Void> updateCouponPolicy(@PathVariable Long policyId, @Valid @RequestBody UpdateCouponPolicyDto updateCouponPolicyDto) {
        couponPolicyService.updateCouponPolicy(policyId, updateCouponPolicyDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deleteCouponPolicy(@PathVariable Long policyId) {
        couponPolicyService.deleteCouponPolicyById(policyId);
        return ResponseEntity.ok().build();
    }

}
