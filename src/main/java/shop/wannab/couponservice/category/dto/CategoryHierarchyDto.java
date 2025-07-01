package shop.wannab.couponservice.category.dto;


import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CategoryHierarchyDto {

    private Long id;
    private String name;
    private List<CategoryHierarchyDto> children;

}