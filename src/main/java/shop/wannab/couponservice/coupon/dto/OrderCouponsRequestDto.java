package shop.wannab.couponservice.coupon.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCouponsRequestDto {
    private List<Long> bookIds;
}
