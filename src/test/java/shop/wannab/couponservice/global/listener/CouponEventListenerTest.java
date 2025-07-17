package shop.wannab.couponservice.global.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.couponservice.coupon.service.CouponService;

@ExtendWith(MockitoExtension.class)
class CouponEventListenerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CouponEventListener couponEventListener;

    @Test
    @DisplayName("회원가입 이벤트 수신 시 웰컴 쿠폰 발급 성공")
    void handleUserSignedUpEvent_Success() {
        Long userId = 1L;

        couponEventListener.handleUserSignedUpEvent(String.valueOf(userId));

        verify(couponService, times(1)).issueWelcomeCouponForNewUser(userId);
    }

    @Test
    @DisplayName("웰컴 쿠폰 발급 중 예외 발생 시에도 이벤트 리스너는 정상 종료")
    void handleUserSignedUpEvent_GracefulFailure() {
        String userId = "1";
        doThrow(new RuntimeException("Test Exception"))
            .when(couponService).issueWelcomeCouponForNewUser(Long.parseLong(userId));

        couponEventListener.handleUserSignedUpEvent(userId);

        verify(couponService, times(1)).issueWelcomeCouponForNewUser(Long.parseLong(userId));
    }
}
