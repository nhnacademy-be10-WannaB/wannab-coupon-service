package shop.wannab.couponservice.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.enums.DiscountType;

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
