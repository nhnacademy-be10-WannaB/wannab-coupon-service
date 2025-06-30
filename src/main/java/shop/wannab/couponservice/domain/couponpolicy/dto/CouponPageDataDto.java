package shop.wannab.couponservice.domain.couponpolicy.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.category.category.dto.CategoryHierarchyDto;

@Getter
@Setter
@AllArgsConstructor
public class CouponPageDataDto {
    private List<CategoryHierarchyDto> categoryHierarchy;
    private List<CouponPolicyResponseDto> couponPolicies;
}
