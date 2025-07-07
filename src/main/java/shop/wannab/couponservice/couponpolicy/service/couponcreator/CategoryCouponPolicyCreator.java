package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetCategoryRepository;

@Component
@RequiredArgsConstructor
public class CategoryCouponPolicyCreator implements CouponPolicyCreator {

    private final CouponPolicyRepository couponPolicyRepository;
    private final PolicyTargetCategoryRepository policyTargetCategoryRepository;

    @Override
    public boolean supports(String couponType) {
        return "CATEGORY".equals(couponType);
    }

    @Override
    public void createCouponPolicy(CreateCouponPolicyDto request) {
        long categoryId = request.getTargetCategoryId();
        if (categoryId <= 0) {
            throw new CouponPolicyException(CouponPolicyErrorCode.INVALID_CATEGORY_ID);
        }

        if (policyTargetCategoryRepository.findByCategoryId(categoryId).isPresent()) {
            throw new CouponPolicyException(CouponPolicyErrorCode.CATEGORY_POLICY_ALREADY_EXISTS);
        }

        CouponPolicy couponPolicy = buildBasePolicy(request);
        couponPolicy.setCouponType(CouponType.CATEGORY);
        CouponPolicy savedPolicy = couponPolicyRepository.save(couponPolicy);

        PolicyTargetCategory policyTargetCategory = PolicyTargetCategory.builder()
                .categoryId(categoryId)
                .couponPolicy(savedPolicy)
                .build();
        policyTargetCategoryRepository.save(policyTargetCategory);
    }

    private CouponPolicy buildBasePolicy(CreateCouponPolicyDto request) {
        return CouponPolicy.builder()
                .couponPolicyName(request.getName())
                .discountType(DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minPurchase(request.getMinPurchase())
                .validDays(request.getValidDays())
                .fixedStartDate(request.getStartDate())
                .fixedEndDate(request.getEndDate())
                .policyStatus(PolicyStatus.ACTIVE).build();
    }
}
