package shop.wannab.couponservice.coupon.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponUsageRequestDto {

    private Long orderId;

    // 사용된 쿠폰 목록
    private List<UsedCouponInfo> usedCoupons;

    @Getter
    @Setter
    public static class UsedCouponInfo {
        private Long couponId;

        private Long bookId;
    }
}
