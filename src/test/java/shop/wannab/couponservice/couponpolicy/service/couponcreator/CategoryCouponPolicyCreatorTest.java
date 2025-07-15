package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetCategoryRepository;


@ExtendWith(MockitoExtension.class)
public class CategoryCouponPolicyCreatorTest {

    @Mock
    private CouponPolicyRepository couponPolicyRepository;
    @Mock
    private PolicyTargetCategoryRepository policyTargetCategoryRepository;

    @InjectMocks
    private CategoryCouponPolicyCreator categoryCouponPolicyCreator;

    @Test
    @DisplayName("지원하는 타입(CATEGORY)이 들어오면 True를 반환한다")
    void supports_WithCategoryType_ShouldReturnTrue() {
        boolean result = categoryCouponPolicyCreator.supports("CATEGORY");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("지원하지 않는 타입이 들어오면 false를 반환한다")
    void supports_WithOtherType_ShouldReturnFalse() {
        boolean result = categoryCouponPolicyCreator.supports("BOOK");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("카테고리 쿠폰 생성에 성공한다")
    void createCouponPolicy_Success() {
        long categoryId = 201L;
        CreateCouponPolicyDto request = createCategoryCouponRequest(categoryId);

        when(policyTargetCategoryRepository.findByCategoryIdAndCouponPolicy_PolicyStatus(categoryId, PolicyStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(couponPolicyRepository.save(any(CouponPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryCouponPolicyCreator.createCouponPolicy(request);

        ArgumentCaptor<PolicyTargetCategory> targetCategoryCaptor = ArgumentCaptor.forClass(PolicyTargetCategory.class);
        verify(policyTargetCategoryRepository).save(targetCategoryCaptor.capture());

        PolicyTargetCategory savedTargetCategory = targetCategoryCaptor.getValue();
        assertThat(savedTargetCategory.getCategoryId()).isEqualTo(categoryId);
        assertThat(savedTargetCategory.getCouponPolicy()).isNotNull();
        assertThat(savedTargetCategory.getCouponPolicy().getCouponType()).isEqualTo(CouponType.CATEGORY);
    }

    @Test
    @DisplayName("유효하지 않은 카테고리 ID(0 이하)로 생성 요청 시 INVALID_CATEGORY_ID 예외를 던진다")
    void createCouponPolicy_WithInvalidCategoryId_ShouldThrowException() {
        CreateCouponPolicyDto request = createCategoryCouponRequest(0L);

        CouponPolicyException exception = assertThrows(CouponPolicyException.class, () -> {
            categoryCouponPolicyCreator.createCouponPolicy(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(CouponPolicyErrorCode.INVALID_CATEGORY_ID);
        verify(couponPolicyRepository, never()).save(any());
        verify(policyTargetCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 해당 카테고리 쿠폰 정책이 존재할 경우 CATEGORY_POLICY_ALREADY_EXISTS 예외를 던진다")
    void createCouponPolicy_WhenPolicyForCategoryAlreadyExists_ShouldThrowException() {
        long categoryId = 201L;
        CreateCouponPolicyDto request = createCategoryCouponRequest(categoryId);

        when(policyTargetCategoryRepository.findByCategoryIdAndCouponPolicy_PolicyStatus(categoryId, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(new PolicyTargetCategory()));

        CouponPolicyException exception = assertThrows(CouponPolicyException.class, () -> {
            categoryCouponPolicyCreator.createCouponPolicy(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(CouponPolicyErrorCode.CATEGORY_POLICY_ALREADY_EXISTS);
        verify(couponPolicyRepository, never()).save(any());
    }

    private CreateCouponPolicyDto createCategoryCouponRequest(long categoryId) {
        CreateCouponPolicyDto request = new CreateCouponPolicyDto();
        request.setName("IT 카테고리 쿠폰");
        request.setDiscountType("PERCENT");
        request.setDiscountValue(10);
        request.setTargetCategoryId(categoryId);
        return request;
    }
}