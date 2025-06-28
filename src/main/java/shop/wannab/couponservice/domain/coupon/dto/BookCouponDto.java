package shop.wannab.couponservice.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.enums.DiscountType;

@Getter
@Setter
@AllArgsConstructor
public class BookCouponDto {
    private Long couponId;
    private String couponName;
    private int discountValue;
    private DiscountType discountType;
}
