package shop.wannab.couponservice.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;

@Getter
@Setter
@AllArgsConstructor
public class BookCouponDto {
    private Long couponId;
    private String couponName;
    private int discountValue;
    private DiscountType discountType;
}
