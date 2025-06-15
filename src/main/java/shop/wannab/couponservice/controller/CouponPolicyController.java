package shop.wannab.couponservice.controller;


import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.domain.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.service.CouponPolicyService;

@RestController
@RequestMapping("/api/admin/coupon_policies")
public class CouponPolicyController {
    private final CouponPolicyService couponPolicyService;

    @Autowired
    public CouponPolicyController(CouponPolicyService couponPolicyService) {
        this.couponPolicyService = couponPolicyService;
    }

    @PostMapping
    public ResponseEntity<Void> createCouponPolicy(@Valid @RequestBody CreateCouponPolicyDto createCouponPolicyDto) {
        CouponPolicy createdPolicy = couponPolicyService.createCouponPolicy(createCouponPolicyDto);

        URI location = URI.create("/api/coupon-policies/" + createdPolicy.getCouponPolicyId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<CouponPolicyResponseDto>> getAllCouponPolicies() {
        List<CouponPolicyResponseDto> policies = couponPolicyService.getCouponPolicies();
        return ResponseEntity.ok(policies);
    }

}
