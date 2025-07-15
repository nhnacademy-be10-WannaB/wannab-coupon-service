package shop.wannab.couponservice.couponpolicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetBookRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetCategoryRepository;
import shop.wannab.couponservice.couponpolicy.service.couponcreator.CouponPolicyCreator;
import shop.wannab.couponservice.couponpolicy.service.couponcreator.CouponPolicyCreatorFactory;

@ExtendWith(MockitoExtension.class)
public class CouponPolicyServiceTest {

    @Mock
    private CouponPolicyCreatorFactory couponPolicyCreatorFactory;
    @Mock
    private CouponPolicyRepository couponPolicyRepository;
    @Mock
    private PolicyTargetBookRepository policyTargetBookRepository;
    @Mock
    private PolicyTargetCategoryRepository policyTargetCategoryRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private CouponRepositoryImpl couponRepositoryImpl;
    @Mock
    private BookServiceClient bookServiceClient;

    @InjectMocks
    private CouponPolicyService couponPolicyService;

    @Test
    @DisplayName("일반 쿠폰 정책 생성 요청 시 적절한 Creator를 찾아 로직을 실행")
    void createWelcomeCouponPolicyTest(){
        CreateCouponPolicyDto request = new CreateCouponPolicyDto();
        request.setCouponType("NORMAL");
        request.setName("웰컴 쿠폰");
        request.setDiscountType("FIXED");
        request.setMinPurchase(10000);
        request.setDiscountValue(3000);
        request.setValidDays(30);
        request.setWelcome(true);

        CouponPolicyCreator mockCreator = mock(CouponPolicyCreator.class);
        when(couponPolicyCreatorFactory.findCreator(request.getCouponType())).thenReturn(mockCreator);
        couponPolicyService.createCouponPolicy(request);
        verify(couponPolicyCreatorFactory, times(1)).findCreator(request.getCouponType());
        verify(mockCreator, times(1)).createCouponPolicy(request);

    }

    @Test
    @DisplayName("활성화된 모든 쿠폰 정책 정보를 올바르게 조회하여 Dto로 변환한다")
    void getCouponPolicies_SuccessTest() {
        CouponPolicy bookCouponPolicy = new CouponPolicy(
                1L, "책 쿠폰", CouponType.BOOK, DiscountType.FIXED,
                3000, 0, 0, 0, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31),
                PolicyStatus.ACTIVE);

        CouponPolicy categoryCouponPolicy = new CouponPolicy(
                2L, "카테고리 쿠폰", CouponType.CATEGORY, DiscountType.FIXED,
                3000, 0, 0, 0, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31),
                PolicyStatus.ACTIVE);

        List<CouponPolicy> activePolicies = List.of(bookCouponPolicy, categoryCouponPolicy);
        List<Long> policyIds = List.of(1L, 2L);
        List<Long> bookIds = List.of(101L);
        List<Long> categoryIds = List.of(201L);

        when(couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE)).thenReturn(activePolicies);
        when(policyTargetBookRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIds))
                .thenReturn(List.of(new PolicyTargetBook(1L, 101L, bookCouponPolicy)));
        when(policyTargetCategoryRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIds))
                .thenReturn(List.of(new PolicyTargetCategory(1L, 201L, categoryCouponPolicy)));
        when(bookServiceClient.getBookNames(bookIds)).thenReturn(Map.of(101L, "JPA 프로그래밍"));
        when(bookServiceClient.getCategoryNames(categoryIds)).thenReturn(Map.of(201L, "컴퓨터/IT"));

        List<CouponPolicyResponseDto> result = couponPolicyService.getCouponPolicies();

        assertThat(result).hasSize(2);

        CouponPolicyResponseDto bookResultDto = result.stream()
                .filter(dto -> dto.getId() == 1L)
                .findFirst()
                .orElseThrow();
        assertThat(bookResultDto.getPurchaseTerm()).isEqualTo("JPA 프로그래밍 구매시");

        CouponPolicyResponseDto categoryResultDto = result.stream()
                .filter(dto -> dto.getId() == 2L)
                .findFirst()
                .orElseThrow();
        assertThat(categoryResultDto.getPurchaseTerm()).isEqualTo("컴퓨터/IT 카테고리 도서 구매시");

        verify(couponPolicyRepository, times(1)).findByPolicyStatus(PolicyStatus.ACTIVE);
        verify(bookServiceClient, times(1)).getBookNames(bookIds);
        verify(bookServiceClient, times(1)).getCategoryNames(categoryIds);
    }

    @Test
    @DisplayName("활성화된 쿠폰 정책이 없다면 빈 리스트를 반환한다")
    void getCouponPolicies_EmptyTest(){
        when(couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE)).thenReturn(new ArrayList<>());

        List<CouponPolicyResponseDto> result = couponPolicyService.getCouponPolicies();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(policyTargetBookRepository, times(0)).findAllByCouponPolicy_CouponPolicyIdIn(anyList());
        verify(bookServiceClient, times(0)).getBookNames(anyList());
    }
    @Test
    @DisplayName("쿠폰 정책의 대상(책,카테고리) 정보가 없을 경우, 이름 없이 DTO를 생성한다")
    void getCouponPolicies_WhenTargetIsMissing() {
        CouponPolicy bookPolicy = new CouponPolicy(
                1L, "책 전용 쿠폰", CouponType.BOOK, DiscountType.FIXED,
                3000, 0, 0, 0, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10),
                PolicyStatus.ACTIVE);

        CouponPolicy categoryPolicy = new CouponPolicy(
                2L, "카테고리 전용 쿠폰", CouponType.CATEGORY, DiscountType.FIXED,
                3000, 0, 0, 0, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10),
                PolicyStatus.ACTIVE);


        List<CouponPolicy> activePolicies = List.of(bookPolicy, categoryPolicy);
        when(couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE)).thenReturn(activePolicies);

        when(policyTargetBookRepository.findAllByCouponPolicy_CouponPolicyIdIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(policyTargetCategoryRepository.findAllByCouponPolicy_CouponPolicyIdIn(anyList()))
                .thenReturn(Collections.emptyList());

        List<CouponPolicyResponseDto> result = couponPolicyService.getCouponPolicies();

        assertThat(result).hasSize(2);

        CouponPolicyResponseDto bookDto = result.stream().filter(d -> d.getId() == 1L).findFirst().orElseThrow();
        assertThat(bookDto.getPurchaseTerm()).isEqualTo("null 구매시");

        CouponPolicyResponseDto categoryDto = result.stream().filter(d -> d.getId() == 2L).findFirst().orElseThrow();
        assertThat(categoryDto.getPurchaseTerm()).isEqualTo("null 카테고리 도서 구매시");
    }

    @Test
    @DisplayName("쿠폰 타입이 BOOK이나 CATEGORY가 아닐 경우(WELCOME 등), 대상 이름 없이 DTO를 생성한다")
    void getCouponPolicies_WhenCouponTypeIsNeitherBookNorCategory() {
        CouponPolicy welcomePolicy = new CouponPolicy(
                3L, "신규가입 쿠폰", CouponType.WELCOME, DiscountType.FIXED,
                2000, 10000, 10000, 0,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                PolicyStatus.ACTIVE);

        List<CouponPolicy> activePolicies = List.of(welcomePolicy);

        when(couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE)).thenReturn(activePolicies);
        when(policyTargetBookRepository.findAllByCouponPolicy_CouponPolicyIdIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(policyTargetCategoryRepository.findAllByCouponPolicy_CouponPolicyIdIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(bookServiceClient.getBookNames(anyList())).thenReturn(Collections.emptyMap());
        when(bookServiceClient.getCategoryNames(anyList())).thenReturn(Collections.emptyMap());


        List<CouponPolicyResponseDto> result = couponPolicyService.getCouponPolicies();


        assertThat(result).hasSize(1);
        CouponPolicyResponseDto resultDto = result.get(0);

        assertThat(resultDto.getPurchaseTerm()).isEqualTo("10,000원 이상");
        assertThat(resultDto.isAutoIssue()).isTrue();
    }

    @Test
    @DisplayName("활성화된 쿠폰 삭제 시 논리적 삭제로 대체")
    void deleteCouponPolicyTest() {
        long policyId = 1L;

        CouponPolicy couponPolicy = new CouponPolicy(
                1L, "일반 쿠폰", CouponType.CUSTOM, DiscountType.FIXED,
                3000, 0, 0, 0, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31),
                PolicyStatus.ACTIVE);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(couponPolicy));

        couponPolicyService.deleteCouponPolicyById(policyId);

        verify(couponPolicyRepository, times(1)).save(couponPolicy);

        assertThat(couponPolicy.getPolicyStatus()).isEqualTo(PolicyStatus.DELETED);

    }

    @Test
    @DisplayName("책과 카테고리 쿠폰이 모두 존재할 경우, 모든 발급 가능한 쿠폰을 조회한다")
    void findIssuablePoliciesForBook_WhenAllCouponsExist() {
        Long bookId = 101L;
        List<Long> categoryIds = List.of(10L, 1L);

        CouponPolicy bookPolicy = new CouponPolicy(
                1L, "책 전용 쿠폰", CouponType.BOOK, DiscountType.FIXED,
                3000, 0, 0, 0, null, null,
                PolicyStatus.ACTIVE);
        PolicyTargetBook targetBook = new PolicyTargetBook(1L,bookId, bookPolicy);
        when(policyTargetBookRepository.findByBookId(bookId)).thenReturn(List.of(targetBook));

        CouponPolicy categoryPolicy = new CouponPolicy(
                2L, "IT 카테고리 쿠폰", CouponType.CATEGORY, DiscountType.PERCENT,
                10, 0, 0, 0, null, null,
                PolicyStatus.ACTIVE);
        List<CouponPolicy> categoryPolicies = List.of(categoryPolicy);

        when(bookServiceClient.getAncestorCategoryIds(bookId)).thenReturn(categoryIds);
        when(couponPolicyRepository.findActivePoliciesForCategoryIds(anyList(), any(PolicyStatus.class))).thenReturn(categoryPolicies);


        List<IssuableCouponPolicyDto> result = couponPolicyService.findIssuablePoliciesForBook(bookId);


        assertThat(result).hasSize(2);

        List<Long> resultPolicyIds = result.stream()
                .map(IssuableCouponPolicyDto::getCouponPolicyId)
                .collect(Collectors.toList());
        assertThat(resultPolicyIds).containsExactlyInAnyOrder(1L, 2L);

        verify(policyTargetBookRepository, times(1)).findByBookId(bookId);
    }

    @Test
    @DisplayName("책 전용 쿠폰이 만료되었을 경우, 카테고리 쿠폰만 조회한다")
    void findIssuablePoliciesForBook_WhenBookCouponIsExpired() {
        Long bookId = 101L;
        List<Long> categoryIds = List.of(10L, 1L);

        CouponPolicy inactiveBookPolicy = new CouponPolicy(
                1L, "비활성 책 쿠폰", CouponType.BOOK, DiscountType.FIXED,
                3000, 0, 0, 0, null, null,
                PolicyStatus.DELETED);

        PolicyTargetBook targetBook = new PolicyTargetBook(1L,bookId, inactiveBookPolicy);
        when(policyTargetBookRepository.findByBookId(bookId)).thenReturn(List.of(targetBook));


        CouponPolicy categoryCouponPolicy = new CouponPolicy(
                2L, "카테고리 쿠폰", CouponType.CATEGORY, DiscountType.FIXED,
                3000, 0, 0, 0, null, null,
                PolicyStatus.ACTIVE);

        when(bookServiceClient.getAncestorCategoryIds(bookId)).thenReturn(categoryIds);
        when(couponPolicyRepository.findActivePoliciesForCategoryIds(anyList(), any(PolicyStatus.class))).thenReturn(List.of(categoryCouponPolicy));

        List<IssuableCouponPolicyDto> result = couponPolicyService.findIssuablePoliciesForBook(bookId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCouponPolicyId()).isEqualTo(2L);

        verify(policyTargetBookRepository, times(1)).findByBookId(bookId);
    }

    @Test
    @DisplayName("책에 연결된 카테고리가 빈 리스트일 경우, 카테고리 쿠폰 조회를 시도하지 않는다")
    void findIssuablePoliciesForBook_WhenCategoryListIsEmpty() {
        Long bookId = 101L;

        when(policyTargetBookRepository.findByBookId(bookId)).thenReturn(Collections.emptyList());

        when(bookServiceClient.getAncestorCategoryIds(bookId)).thenReturn(Collections.emptyList());

        List<IssuableCouponPolicyDto> result = couponPolicyService.findIssuablePoliciesForBook(bookId);

        assertThat(result).isEmpty();

        verify(couponPolicyRepository, never()).findActivePoliciesForCategoryIds(anyList(),eq(PolicyStatus.ACTIVE));
    }

    @Test
    @DisplayName("책에 연결된 카테고리가 null일 경우, 카테고리 쿠폰 조회를 시도하지 않는다")
    void findIssuablePoliciesForBook_WhenCategoryListIsNull() {
        Long bookId = 101L;

        when(policyTargetBookRepository.findByBookId(bookId)).thenReturn((Collections.emptyList()));

        when(bookServiceClient.getAncestorCategoryIds(bookId)).thenReturn(null);

        List<IssuableCouponPolicyDto> result = couponPolicyService.findIssuablePoliciesForBook(bookId);

        assertThat(result).isEmpty();

        verify(couponPolicyRepository, never()).findActivePoliciesForCategoryIds(anyList(),eq(PolicyStatus.ACTIVE));
    }

}
