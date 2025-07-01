package shop.wannab.couponservice.coupon.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TryApplyCouponsRequestDto {
    Map<Long,Long> couponAndBookIds;
}
