package shop.wannab.couponservice.controller;


import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.couponservice.domain.couponpolicy.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.CreateCouponPolicyDto;
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

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deleteCouponPolicy(@PathVariable Long policyId) {
        couponPolicyService.deleteCouponPolicyById(policyId);
        return ResponseEntity.ok().build();
    }
}
