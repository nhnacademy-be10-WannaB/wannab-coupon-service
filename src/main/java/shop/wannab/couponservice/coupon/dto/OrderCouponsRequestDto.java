package shop.wannab.couponservice.coupon.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCouponsRequestDto {
    private List<Long> bookIds;
}
