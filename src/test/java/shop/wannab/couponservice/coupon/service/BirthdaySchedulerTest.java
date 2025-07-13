package shop.wannab.couponservice.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BirthdaySchedulerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private BirthdayScheduler birthdayScheduler;

    @Test
    @DisplayName("생일 쿠폰 스케줄러가 현재 월에 대해 CouponService를 호출한다")
    void issueBirthdayCoupons_shouldCallCouponServiceWithCurrentMonth() {
        birthdayScheduler.issueBirthdayCoupons();

        ArgumentCaptor<Integer> monthCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(couponService, times(1)).issueBirthdayCoupon(monthCaptor.capture());

        int expectedMonth = LocalDate.now().getMonthValue();
        assertThat(monthCaptor.getValue()).isEqualTo(expectedMonth);
    }
}
