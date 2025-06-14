package shop.wannab.couponservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.PolicyTargetBook;
import shop.wannab.couponservice.domain.PolicyTargetCategory;
import shop.wannab.couponservice.domain.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.domain.dto.CreateCouponPolicyDto;
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
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CouponPolicyResponseDto convertToDto(CouponPolicy policy) {
        CouponPolicyResponseDto dto = new CouponPolicyResponseDto();

        dto.setId(policy.getCouponPolicyId());
        dto.setName(policy.getCouponPolicyName());
        dto.setDiscountType(policy.getDiscountType() == DiscountType.FIXED ? "정액" : "정률");
        dto.setMinPurchase(String.format("%,d원 이상", policy.getMinPurchase()));

        // 할인 정보 포맷팅
        if (policy.getDiscountType() == DiscountType.FIXED) {
            dto.setDiscount(String.format("%,d원", policy.getDiscountValue()));
        } else {
            dto.setDiscount(String.format("%d%%", policy.getDiscountValue()));
        }

        if (policy.getValidDays() > 0) {
            dto.setPeriod(String.format("발급 후 %d일", policy.getValidDays()));
        } else {
            dto.setPeriod(String.format("%s ~ %s", policy.getFixedStartDate(), policy.getFixedEndDate()));
        }

        // 자동 발급 조건 포맷팅
        List<String> autoIssueTypes = new ArrayList<>();
        if (policy.getPolicyRule() == PolicyRule.WELCOME) {
            autoIssueTypes.add("회원가입");
        }
        if (policy.getPolicyRule() == PolicyRule.BIRTHDAY) {
            autoIssueTypes.add("생일");
        }
        dto.setAutoIssue(String.join(", ", autoIssueTypes));

        return dto;
    }
}
