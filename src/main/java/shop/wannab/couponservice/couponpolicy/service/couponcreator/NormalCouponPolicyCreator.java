package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;

@Component
@RequiredArgsConstructor
public class NormalCouponPolicyCreator implements CouponPolicyCreator {

    private final CouponPolicyRepository couponPolicyRepository;

    @Override
    public boolean supports(String couponType) {
        return "NORMAL".equalsIgnoreCase(couponType);
    }

    @Override
    public void createCouponPolicy(CreateCouponPolicyDto request) {
        CouponType newCouponType;
        if (request.isBirthday()) {
            newCouponType = CouponType.BIRTHDAY;
        } else if (request.isWelcome()) {
            newCouponType = CouponType.WELCOME;
        } else {
            newCouponType = CouponType.CUSTOM;
        }

        if (couponPolicyRepository.findByCouponTypeAndPolicyStatus(newCouponType, PolicyStatus.ACTIVE).isPresent()) {
            throw new CouponPolicyException(CouponPolicyErrorCode.POLICY_ALREADY_EXISTS);
        }

        CouponPolicy couponPolicy = buildBasePolicy(request);
        couponPolicy.setCouponType(newCouponType);
        couponPolicyRepository.save(couponPolicy);
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
