package shop.wannab.couponservice.service;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BirthdayScheduler {
    private final CouponService couponService;

    public BirthdayScheduler(CouponService couponService) {
        this.couponService = couponService;
    }

    @Scheduled(cron="0 0 3 1 * ?")
    public void issueBirthdayCoupons() {
        int month = LocalDate.now().getMonthValue();
        couponService.issueBirthdayCoupon(month);
    }
}
