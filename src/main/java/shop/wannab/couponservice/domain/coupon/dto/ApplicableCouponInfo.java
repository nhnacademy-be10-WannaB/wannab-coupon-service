package shop.wannab.couponservice.domain.coupon.dto;

import shop.wannab.couponservice.domain.coupon.Coupon;

public record ApplicableCouponInfo(
        Coupon coupon,
        Long targetBookId
) {
}
