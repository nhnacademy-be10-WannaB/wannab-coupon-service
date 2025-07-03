package shop.wannab.couponservice.coupon.dto;

import shop.wannab.couponservice.coupon.entity.Coupon;

public record ApplicableCouponInfo(
        Coupon coupon,
        Long targetBookId
) {
}
