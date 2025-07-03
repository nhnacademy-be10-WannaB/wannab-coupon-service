package shop.wannab.couponservice.couponpolicy.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetBookRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetCategoryRepository;

@Service
public class CouponPolicyService {
    private final CouponPolicyRepository couponPolicyRepository;
    private final PolicyTargetBookRepository policyTargetBookRepository;
    private final PolicyTargetCategoryRepository policyTargetCategoryRepository;
    private final CategoryService categoryService;
    private final CouponRepositoryImpl couponRepositoryImpl;
    private final BookServiceClient bookServiceClient;

    public CouponPolicyService(
            CouponPolicyRepository couponPolicyRepository,
            PolicyTargetBookRepository policyTargetBookRepository,
            PolicyTargetCategoryRepository policyTargetCategoryRepository,
            CouponRepositoryImpl couponRepositoryImpl,
            CategoryService categoryService,
            BookServiceClient bookServiceClient
    ) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.policyTargetBookRepository = policyTargetBookRepository;
        this.policyTargetCategoryRepository = policyTargetCategoryRepository;
        this.categoryService = categoryService;
        this.couponRepositoryImpl = couponRepositoryImpl;
        this.bookServiceClient = bookServiceClient;
    }

    @Transactional
    public void createCouponPolicy(CreateCouponPolicyDto request) {
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

        CouponType newCouponType;


        if (request.getCouponType().equals("NORMAL")) {
            if (request.isBirthday()) {
                newCouponType = CouponType.BIRTHDAY;
            } else if (request.isWelcome()) {
                newCouponType = CouponType.WELCOME;
            } else {
                newCouponType = CouponType.CUSTOM;
            }
            couponPolicy.setCouponType(newCouponType);

            if (couponPolicyRepository.findByCouponTypeAndPolicyStatus(newCouponType, PolicyStatus.ACTIVE).isPresent()) {
                throw new CouponPolicyException(CouponPolicyErrorCode.POLICY_ALREADY_EXISTS);
            }
            couponPolicyRepository.save(couponPolicy);

        } else if (request.getCouponType().equals("BOOK")) {
            newCouponType = CouponType.BOOK;

            long bookId = request.getTargetBookId();
            if (bookId <= 0) {
                throw new CouponPolicyException(CouponPolicyErrorCode.INVALID_BOOK_ID);
            }

            if (policyTargetBookRepository.findByBookId(bookId).isPresent()) {
                throw new CouponPolicyException(CouponPolicyErrorCode.BOOK_POLICY_ALREADY_EXISTS);
            }

            couponPolicy.setCouponType(newCouponType);
            couponPolicyRepository.save(couponPolicy);
            createPolicyTargetBook(bookId, couponPolicy);

        } else {
            newCouponType = CouponType.CATEGORY;

            long categoryId = request.getTargetCategoryId();
            if (categoryId <= 0) {
                throw new CouponPolicyException(CouponPolicyErrorCode.INVALID_CATEGORY_ID);
            }

            if (policyTargetCategoryRepository.findByCategoryId(categoryId).isPresent()) {
                throw new CouponPolicyException(CouponPolicyErrorCode.CATEGORY_POLICY_ALREADY_EXISTS);
            }

            couponPolicy.setCouponType(newCouponType);
            couponPolicyRepository.save(couponPolicy);
            createPolicyTargetCategory(categoryId, couponPolicy);
        }
    }

    private void createPolicyTargetBook(long bookId, CouponPolicy couponPolicy) {
        PolicyTargetBook policyTargetBook = PolicyTargetBook.builder()
                .bookId(bookId)
                .couponPolicy(couponPolicy).build();
        policyTargetBookRepository.save(policyTargetBook);
    }

    private void createPolicyTargetCategory(long categoryId, CouponPolicy couponPolicy) {
        PolicyTargetCategory policyTargetCategory = PolicyTargetCategory.builder()
                .categoryId(categoryId)
                .couponPolicy(couponPolicy).build();
        policyTargetCategoryRepository.save(policyTargetCategory);
    }

    //쿠폰 정책 목록
    @Transactional(readOnly = true)
    public List<CouponPolicyResponseDto> getCouponPolicies() {
        List<CouponPolicy> activePolicies = couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE);

        Map<Long, Long> policyToBookIdMap = new HashMap<>();
        Map<Long, Long> policyToCategoryIdMap = new HashMap<>();

        List<Long> bookIdsToFetch = new ArrayList<>();
        List<Long> categoryIdsToFetch = new ArrayList<>();

        for (CouponPolicy policy : activePolicies) {
            if (policy.getCouponType() == CouponType.BOOK) {
                Optional<Long> bookIdOptional = policyTargetBookRepository.findBookIdByCouponPolicy(policy);
                if (bookIdOptional.isPresent()) {
                    Long bookId = bookIdOptional.get();
                    bookIdsToFetch.add(bookId);
                    policyToBookIdMap.put(policy.getCouponPolicyId(), bookId);
                }
            } else if (policy.getCouponType() == CouponType.CATEGORY) {
                Optional<Long> categoryIdOptional = policyTargetCategoryRepository.findCategoryIdByCouponPolicy(policy);
                if (categoryIdOptional.isPresent()) {
                    Long categoryId = categoryIdOptional.get();
                    categoryIdsToFetch.add(categoryId);
                    policyToCategoryIdMap.put(policy.getCouponPolicyId(), categoryId);
                }
            }
        }

        Map<Long, String> bookNamesMap = bookServiceClient.getBookNames(bookIdsToFetch);
        Map<Long, String> categoryNamesMap = bookServiceClient.getCategoryNames(categoryIdsToFetch);

        List<CouponPolicyResponseDto> responseDtos = new ArrayList<>();
        for (CouponPolicy policy : activePolicies) {
            String bookName = null;
            String categoryName = null;

            if (policy.getCouponType() == CouponType.BOOK) {
                Long bookId = policyToBookIdMap.get(policy.getCouponPolicyId());
                if (bookId != null) {
                    bookName = bookNamesMap.getOrDefault(bookId, "알 수 없는 책");
                }
            } else if (policy.getCouponType() == CouponType.CATEGORY) {
                Long categoryId = policyToCategoryIdMap.get(policy.getCouponPolicyId());
                if (categoryId != null) {
                    categoryName = categoryNamesMap.getOrDefault(categoryId, "알 수 없는 카테고리");
                }
            }

            responseDtos.add(CouponPolicyResponseDto.from(policy, bookName, categoryName));
        }

        return responseDtos;
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
        List<IssuableCouponPolicyDto> issuableCouponPolicyDtoList = new ArrayList<>();
        PolicyTargetBook policyTargetBook = policyTargetBookRepository.findByBookId(bookId).orElse(null);
        Long categoryId = bookServiceClient.getCategoryId(bookId);
        PolicyTargetCategory policyTargetCategory = policyTargetCategoryRepository.findById(categoryId).orElse(null);
        if (policyTargetBook != null) {
            if (policyTargetBook.getCouponPolicy().getFixedEndDate().isAfter(LocalDate.now())) {
                issuableCouponPolicyDtoList.add(new IssuableCouponPolicyDto(policyTargetBook.getCouponPolicy()));
            }
        }
        if (policyTargetCategory != null) {
            List<Long> ancestorCategoryIds = categoryService.getAncestorCategoryIds(categoryId);
            List<CouponPolicy> couponPolicies = couponRepositoryImpl.findActiveCouponPolicies(ancestorCategoryIds);
            for (CouponPolicy couponPolicy : couponPolicies) {
                issuableCouponPolicyDtoList.add(new IssuableCouponPolicyDto(couponPolicy));
            }
        }
        return issuableCouponPolicyDtoList;
    }
}
