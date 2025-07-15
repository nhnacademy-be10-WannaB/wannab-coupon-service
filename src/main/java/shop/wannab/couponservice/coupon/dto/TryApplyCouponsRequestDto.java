package shop.wannab.couponservice.coupon.dto;

import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TryApplyCouponsRequestDto {
    Map<Long,Long> couponAndBookIds;
}
