package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;

@ExtendWith(MockitoExtension.class)
class NormalCouponPolicyCreatorTest {
    @Mock
    private CouponPolicyRepository couponPolicyRepository;

    @InjectMocks
    private NormalCouponPolicyCreator normalCouponPolicyCreator;

    @Test
    @DisplayName("지원하는 타입이 들어오면 True 반환")
    void supports_WithInvalidType_ShouldReturnTrue() {
        boolean result = normalCouponPolicyCreator.supports("NORMAL");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("지원하지 않는 타입이 들어오면 false를 반환")
    void supports_WithInvalidType_ShouldReturnFalse() {
        boolean result = normalCouponPolicyCreator.supports("BOOK");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("생일 쿠폰 생성 시, 중복 정책이 없으면 성공적으로 저장한다")
    void createCouponPolicy_ForBirthday_WhenPolicyDoesNotExist() {
        CreateCouponPolicyDto request = new CreateCouponPolicyDto();
        request.setBirthday(true);
        request.setName("생일 축하 쿠폰");
        request.setDiscountType("FIXED");
        request.setDiscountValue(3000);


        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.BIRTHDAY, PolicyStatus.ACTIVE))
                .thenReturn(Optional.empty());

        normalCouponPolicyCreator.createCouponPolicy(request);

        ArgumentCaptor<CouponPolicy> captor = ArgumentCaptor.forClass(CouponPolicy.class);
        verify(couponPolicyRepository, times(1)).save(captor.capture());

        CouponPolicy savedPolicy = captor.getValue();
        assertThat(savedPolicy.getCouponType()).isEqualTo(CouponType.BIRTHDAY);
        assertThat(savedPolicy.getCouponPolicyName()).isEqualTo("생일 축하 쿠폰");
    }

    @Test
    @DisplayName("웰컴 쿠폰 생성 시, 동일한 활성 정책이 이미 존재하면 POLICY_ALREADY_EXISTS 예외를 던진다")
    void createCouponPolicy_ForWelcome_WhenPolicyAlreadyExists_ShouldThrowException() {
        CreateCouponPolicyDto request = new CreateCouponPolicyDto();
        request.setWelcome(true);
        request.setDiscountType("FIXED");

        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(new CouponPolicy()));

        CouponPolicyException exception = assertThrows(CouponPolicyException.class, () -> {
            normalCouponPolicyCreator.createCouponPolicy(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(CouponPolicyErrorCode.POLICY_ALREADY_EXISTS);

        verify(couponPolicyRepository, never()).save(any());
    }

    @Test
    @DisplayName("isBirthday, isWelcome이 모두 false일 경우 CUSTOM 타입으로 생성한다")
    void createCouponPolicy_WhenNoFlagIsSet_ShouldCreateCustomType() {
        CreateCouponPolicyDto request = new CreateCouponPolicyDto();
        request.setName("모든 도서 할인");
        request.setDiscountType("FIXED");
        request.setDiscountValue(1000);
        request.setBirthday(false);
        request.setWelcome(false);

        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.CUSTOM, PolicyStatus.ACTIVE))
                .thenReturn(Optional.empty());

        normalCouponPolicyCreator.createCouponPolicy(request);

        ArgumentCaptor<CouponPolicy> captor = ArgumentCaptor.forClass(CouponPolicy.class);
        verify(couponPolicyRepository).save(captor.capture());

        assertThat(captor.getValue().getCouponType()).isEqualTo(CouponType.CUSTOM);
    }
}