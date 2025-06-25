package shop.wannab.couponservice.domain.category.category.dto;


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

    // JSON의 자식 노드들도 이와 동일한 구조이므로, 재귀적으로 자기 자신을 참조합니다.
    private List<CategoryHierarchyDto> children;

}