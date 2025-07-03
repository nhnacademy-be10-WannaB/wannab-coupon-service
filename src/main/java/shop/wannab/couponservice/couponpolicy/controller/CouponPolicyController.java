package shop.wannab.couponservice.couponpolicy.controller;


import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.couponservice.category.CategoryService;
import shop.wannab.couponservice.category.dto.CategoryHierarchyDto;
import shop.wannab.couponservice.couponpolicy.service.CouponPolicyService;
import shop.wannab.couponservice.couponpolicy.dto.CouponPageDataDto;
import shop.wannab.couponservice.couponpolicy.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;


@RestController
@RequestMapping("/api/admin/coupon_policies")
@RequiredArgsConstructor
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;
    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<Void> createCouponPolicy(@Valid @RequestBody CreateCouponPolicyDto createCouponPolicyDto) {
        couponPolicyService.createCouponPolicy(createCouponPolicyDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<CouponPageDataDto> getAllCouponPolicies() {
        List<CategoryHierarchyDto> categoryHierarchyDtos = categoryService.getCategoryHierarchy();
        List<CouponPolicyResponseDto> policies = couponPolicyService.getCouponPolicies();
        CouponPageDataDto couponPageDataDto = new CouponPageDataDto(categoryHierarchyDtos,policies);
        return ResponseEntity.ok(couponPageDataDto);
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deleteCouponPolicy(@PathVariable Long policyId) {
        couponPolicyService.deleteCouponPolicyById(policyId);
        return ResponseEntity.ok().build();
    }
}
