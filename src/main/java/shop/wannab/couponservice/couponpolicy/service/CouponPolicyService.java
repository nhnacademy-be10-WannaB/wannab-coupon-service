package shop.wannab.couponservice.couponpolicy.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.category.CategoryService;
import shop.wannab.couponservice.client.BookServiceClient;
import shop.wannab.couponservice.coupon.repository.impl.CouponRepositoryImpl;
import shop.wannab.couponservice.couponpolicy.dto.CouponPolicyResponseDto;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.dto.IssuableCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetBookRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetCategoryRepository;
import shop.wannab.couponservice.couponpolicy.service.couponcreator.CouponPolicyCreator;
import shop.wannab.couponservice.couponpolicy.service.couponcreator.CouponPolicyCreatorFactory;

@Service
@RequiredArgsConstructor
public class CouponPolicyService {
    private final CouponPolicyRepository couponPolicyRepository;
    private final PolicyTargetBookRepository policyTargetBookRepository;
    private final PolicyTargetCategoryRepository policyTargetCategoryRepository;
    private final CategoryService categoryService;
    private final CouponRepositoryImpl couponRepositoryImpl;
    private final BookServiceClient bookServiceClient;
    private final CouponPolicyCreatorFactory couponPolicyCreatorFactory;

    @Transactional
    public void createCouponPolicy(CreateCouponPolicyDto request) {
        CouponPolicyCreator creator = couponPolicyCreatorFactory.findCreator(request.getCouponType());

        creator.createCouponPolicy(request);
    }


    //쿠폰 정책 목록
    @Transactional(readOnly = true)
    public List<CouponPolicyResponseDto> getCouponPolicies() {
        List<CouponPolicy> activePolicies = couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE);
        if (activePolicies.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> policyIds = activePolicies.stream()
                .map(CouponPolicy::getCouponPolicyId)
                .collect(Collectors.toList());

        Map<Long, Long> policyToBookIdMap = policyTargetBookRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIds)
                .stream()
                .collect(Collectors.toMap(ptb -> ptb.getCouponPolicy().getCouponPolicyId(), PolicyTargetBook::getBookId));

        Map<Long, Long> policyToCategoryIdMap = policyTargetCategoryRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIds)
                .stream()
                .collect(Collectors.toMap(ptc -> ptc.getCouponPolicy().getCouponPolicyId(), PolicyTargetCategory::getCategoryId));

        List<Long> bookIdsToFetch = new ArrayList<>(policyToBookIdMap.values());
        List<Long> categoryIdsToFetch = new ArrayList<>(policyToCategoryIdMap.values());

        Map<Long, String> bookNamesMap = bookServiceClient.getBookNames(bookIdsToFetch);
        Map<Long, String> categoryNamesMap = bookServiceClient.getCategoryNames(categoryIdsToFetch);

        return activePolicies.stream()
                .map(policy -> {
                    String bookName = null;
                    String categoryName = null;
              if (policy.getCouponType() == CouponType.BOOK) {
                  Long bookId = policyToBookIdMap.get(policy.getCouponPolicyId());
                  if (bookId != null) {
                      bookName = bookNamesMap.getOrDefault(bookId, "알 수 없는 책");
                  }
              } else {
                  if (policy.getCouponType() == CouponType.CATEGORY) {
                      Long categoryId = policyToCategoryIdMap.get(policy.getCouponPolicyId());
                      if (categoryId != null) {
                          categoryName = categoryNamesMap.getOrDefault(categoryId, "알 수 없는 카테고리");
                      }
                  }
              }
                    return CouponPolicyResponseDto.from(policy, bookName, categoryName);
                })
                .collect(Collectors.toList());
    }


    //DB상에서 진짜 삭제는 아니고 논리적 삭제(회원이 쿠폰 내역을 확인 할 때 데이터 자체를 삭제하면 문제가 될 수 있으므로 삭제 상태로 변경)
    @Transactional
    public void deleteCouponPolicyById(long policyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(policyId).orElse(null);
        couponPolicy.setPolicyStatus(PolicyStatus.DELETED);
        couponPolicyRepository.save(couponPolicy);
    }


    @Transactional(readOnly = true)
    public List<IssuableCouponPolicyDto> findIssuablePoliciesForBook(Long bookId) {
        Set<CouponPolicy> finalPolicies = new HashSet<>();

        policyTargetBookRepository.findByBookId(bookId)
                .stream()
                .map(PolicyTargetBook::getCouponPolicy)
                .filter(policy -> policy.getPolicyStatus() == PolicyStatus.ACTIVE)
                .forEach(finalPolicies::add);

        List<Long> categoryIds = bookServiceClient.getAncestorCategoryIds(bookId);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<CouponPolicy> categoryPolicies = couponPolicyRepository.findActivePoliciesForCategoryIds(
                    categoryIds,
                    PolicyStatus.ACTIVE
            );
            finalPolicies.addAll(categoryPolicies);
        }

        return finalPolicies.stream()
                .map(IssuableCouponPolicyDto::new)
                .collect(Collectors.toList());
    }
}
