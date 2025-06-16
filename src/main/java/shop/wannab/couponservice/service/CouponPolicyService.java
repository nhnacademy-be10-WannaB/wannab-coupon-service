package shop.wannab.couponservice.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.PolicyTargetBook;
import shop.wannab.couponservice.domain.PolicyTargetCategory;
import shop.wannab.couponservice.domain.dto.CouponPolicyDetailResponseDto;
import shop.wannab.couponservice.domain.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.domain.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.domain.dto.UpdateCouponPolicyDto;
import shop.wannab.couponservice.domain.enums.DiscountType;
import shop.wannab.couponservice.domain.enums.PolicyRule;
import shop.wannab.couponservice.repository.CouponPolicyRepository;
import shop.wannab.couponservice.repository.CouponRepository;
import shop.wannab.couponservice.repository.PolicyTargetBookRepository;
import shop.wannab.couponservice.repository.PolicyTargetCategoryRepository;

@Service
public class CouponPolicyService {
    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponRepository couponRepository;
    private final PolicyTargetBookRepository policyTargetBookRepository;
    private final PolicyTargetCategoryRepository policyTargetCategoryRepository;

    public CouponPolicyService(
            CouponPolicyRepository couponPolicyRepository,
            CouponRepository couponRepository,
            PolicyTargetBookRepository policyTargetBookRepository,
            PolicyTargetCategoryRepository policyTargetCategoryRepository
            ) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.couponRepository = couponRepository;
        this.policyTargetBookRepository = policyTargetBookRepository;
        this.policyTargetCategoryRepository = policyTargetCategoryRepository;
    }

    @Transactional
    public CouponPolicy createCouponPolicy(CreateCouponPolicyDto request){
        CouponPolicy couponPolicy = CouponPolicy.builder()
                .couponPolicyName(request.getName())
                .discountType(DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minPurchase(request.getMinPurchase())
                .validDays(request.getValidForDays())
                .fixedStartDate(request.getStartDate())
                .fixedEndDate(request.getEndDate()).build();

        if(request.getCouponType().equals("NORMAL")){
            if(request.isBirthday()){
                couponPolicy.setPolicyRule(PolicyRule.BIRTHDAY);
            }
            else if(request.isWelcome()){
                couponPolicy.setPolicyRule(PolicyRule.WELCOME);
            }
            else{
                couponPolicy.setPolicyRule(PolicyRule.CUSTOM);
            }
            couponPolicyRepository.save(couponPolicy);
        }

        else if(request.getCouponType().equals("BOOK")){
            couponPolicyRepository.save(couponPolicy);
            long bookId = request.getBookId();
            createPolicyTargetBook(bookId, couponPolicy);
        }
        else{
            couponPolicyRepository.save(couponPolicy);
            long categoryId = request.getCategoryId();
            createPolicyTargetCategory(categoryId, couponPolicy);
        }


        return couponPolicy;
    }

    private void createPolicyTargetBook(long bookId,CouponPolicy couponPolicy){
        PolicyTargetBook policyTargetBook = PolicyTargetBook.builder()
                .bookId(bookId)
                .couponPolicy(couponPolicy).build();
        policyTargetBookRepository.save(policyTargetBook);
    }

    private void createPolicyTargetCategory(long categoryId,CouponPolicy couponPolicy){
        PolicyTargetCategory policyTargetCategory = PolicyTargetCategory.builder()
                .categoryId(categoryId)
                .couponPolicy(couponPolicy).build();
        policyTargetCategoryRepository.save(policyTargetCategory);
    }

    //쿠폰 정책 목록
    @Transactional
    public List<CouponPolicyResponseDto> getCouponPolicies() {
        List<CouponPolicy> policies = couponPolicyRepository.findAll();

        return policies.stream()
                .map(CouponPolicyResponseDto::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponPolicyDetailResponseDto getCouponPolicyById(long policyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(policyId).orElse(null);
        return CouponPolicyDetailResponseDto.convertToDto(
                Objects.requireNonNull(couponPolicy));

    }

    @Transactional
    public void updateCouponPolicy(long couponPolicyId, UpdateCouponPolicyDto request) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId).orElse(null);
        couponPolicy.setMinPurchase(request.getMinPurchase());
        couponPolicy.setDiscountValue(request.getDiscountValue());
        couponPolicy.setMaxDiscount(request.getMaxDiscount());

        if(request.getValidityType().equals("FIXED")){
            couponPolicy.setValidDays(request.getValidForDays());
        }
        else{
            couponPolicy.setFixedEndDate(request.getEndDate());
        }

        couponPolicyRepository.save(couponPolicy);
    }
}
