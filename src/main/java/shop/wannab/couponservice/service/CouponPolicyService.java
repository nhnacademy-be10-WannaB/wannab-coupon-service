package shop.wannab.couponservice.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.PolicyTargetBook;
import shop.wannab.couponservice.domain.PolicyTargetCategory;
import shop.wannab.couponservice.domain.couponpolicy.dto.CouponPolicyDetailResponseDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.domain.couponpolicy.dto.UpdateCouponPolicyDto;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.DiscountType;
import shop.wannab.couponservice.domain.enums.PolicyStatus;
import shop.wannab.couponservice.repository.CouponPolicyRepository;
import shop.wannab.couponservice.repository.PolicyTargetBookRepository;
import shop.wannab.couponservice.repository.PolicyTargetCategoryRepository;

@Service
public class CouponPolicyService {
    private final CouponPolicyRepository couponPolicyRepository;
    private final PolicyTargetBookRepository policyTargetBookRepository;
    private final PolicyTargetCategoryRepository policyTargetCategoryRepository;

    public CouponPolicyService(
            CouponPolicyRepository couponPolicyRepository,
            PolicyTargetBookRepository policyTargetBookRepository,
            PolicyTargetCategoryRepository policyTargetCategoryRepository
            ) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.policyTargetBookRepository = policyTargetBookRepository;
        this.policyTargetCategoryRepository = policyTargetCategoryRepository;
    }

    @Transactional
    public void createCouponPolicy(CreateCouponPolicyDto request){
        CouponPolicy couponPolicy = CouponPolicy.builder()
                .couponPolicyName(request.getName())
                .discountType(DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minPurchase(request.getMinPurchase())
                .validDays(request.getValidDays())
                .fixedStartDate(request.getStartDate())
                .fixedEndDate(request.getEndDate())
                .policyStatus(PolicyStatus.ACTIVE).build();

        if(request.getCouponType().equals("NORMAL")){
            if(request.isBirthday()){
                couponPolicy.setCouponType(CouponType.BIRTHDAY);
            }
            else if(request.isWelcome()){
                couponPolicy.setCouponType(CouponType.WELCOME);
            }
            else{
                couponPolicy.setCouponType(CouponType.CUSTOM);
            }
            couponPolicyRepository.save(couponPolicy);
        }

        else if(request.getCouponType().equals("BOOK")){
            couponPolicy.setCouponType(CouponType.BOOK);
            couponPolicyRepository.save(couponPolicy);
            long bookId = request.getTargetBookId();
            createPolicyTargetBook(bookId, couponPolicy);
        }
        else{
            couponPolicy.setCouponType(CouponType.CATEGORY);
            couponPolicyRepository.save(couponPolicy);
            long categoryId = request.getTargetCategoryId();
            createPolicyTargetCategory(categoryId, couponPolicy);
        }

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
    @Transactional(readOnly = true)
    public List<CouponPolicyResponseDto> getCouponPolicies() {
        List<CouponPolicy> activePolicies = couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE);

        return activePolicies.stream()
                .map(policy -> {
                    String bookName = null;
                    String categoryName = null;

                    if (policy.getCouponType() == CouponType.BOOK) {

                        Long bookId = findBookIdByCouponPolicy(policy);


                        if (bookId != null) {
                            bookName = CouponPolicyDetailResponseDto.DUMMY_BOOKS.get(bookId);
                        }

                    } else if (policy.getCouponType() == CouponType.CATEGORY) {

                    }

                    return CouponPolicyResponseDto.from(policy, bookName, categoryName);
                })
                .collect(Collectors.toList());
    }

    private Long findBookIdByCouponPolicy(CouponPolicy couponPolicy) {
        return policyTargetBookRepository.findByCouponPolicy(couponPolicy)
                .map(PolicyTargetBook::getBookId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public CouponPolicyDetailResponseDto getCouponPolicyById(long policyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(policyId).orElse(null);
        return CouponPolicyDetailResponseDto.from(
                Objects.requireNonNull(couponPolicy),null,null);
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

    //DB상에서 진짜 삭제는 아니고 논리적 삭제(회원이 쿠폰 내역을 확인 할 때 데이터 자체를 삭제하면 문제가 될 수 있으므로 삭제 상태로 변경)
    @Transactional
    public void deleteCouponPolicyById(long policyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(policyId).orElse(null);
        couponPolicy.setPolicyStatus(PolicyStatus.DELETED);
        couponPolicyRepository.save(couponPolicy);
    }

}
