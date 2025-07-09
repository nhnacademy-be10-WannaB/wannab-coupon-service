package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetBookRepository;

@Component
@RequiredArgsConstructor
public class BookCouponPolicyCreator implements CouponPolicyCreator {

    private final CouponPolicyRepository couponPolicyRepository;
    private final PolicyTargetBookRepository policyTargetBookRepository;

    @Override
    public boolean supports(String couponType) {
        return "BOOK".equalsIgnoreCase(couponType);
    }

    @Override
    public void createCouponPolicy(CreateCouponPolicyDto request) {
        long bookId = request.getTargetBookId();
        if (bookId <= 0) {
            throw new CouponPolicyException(CouponPolicyErrorCode.INVALID_BOOK_ID);
        }

        if (policyTargetBookRepository.findByBookIdAndCouponPolicy_PolicyStatus(bookId,PolicyStatus.ACTIVE).isPresent()) {
            throw new CouponPolicyException(CouponPolicyErrorCode.BOOK_POLICY_ALREADY_EXISTS);
        }

        CouponPolicy couponPolicy = buildBasePolicy(request);
        couponPolicy.setCouponType(CouponType.BOOK);
        CouponPolicy savedPolicy = couponPolicyRepository.save(couponPolicy);

        PolicyTargetBook policyTargetBook = PolicyTargetBook.builder()
                .bookId(bookId)
                .couponPolicy(savedPolicy)
                .build();
        policyTargetBookRepository.save(policyTargetBook);
    }

    private CouponPolicy buildBasePolicy(CreateCouponPolicyDto request) {
        return CouponPolicy.builder()
                .couponPolicyName(request.getName())
                .discountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()))
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minPurchase(request.getMinPurchase())
                .fixedStartDate(request.getStartDate())
                .fixedEndDate(request.getEndDate())
                .policyStatus(PolicyStatus.ACTIVE)
                .build();
    }
}
