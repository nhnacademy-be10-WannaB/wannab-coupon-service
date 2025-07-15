package shop.wannab.couponservice.category.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryHierarchyDto {

    private Long id;
    private String name;
    private List<CategoryHierarchyDto> children;

}