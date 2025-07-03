package shop.wannab.couponservice.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;

//이걸 반환
@Getter
@Setter
@AllArgsConstructor
public class TryApplyCouponsResponseDto {
    private Long couponId;
    private int discountValue;
    private DiscountType discountType;
    private Long bookId;
}
