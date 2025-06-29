package shop.wannab.couponservice.domain.category.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Service;
import shop.wannab.couponservice.client.BookServiceClient;
import shop.wannab.couponservice.domain.category.category.dto.CategoryHierarchyDto;

@Service
public class CategoryService {
    private final BookServiceClient bookServiceClient;

    private Map<Long,CategoryNode> categoryMap;

    public CategoryService(BookServiceClient bookServiceClient) {
        this.bookServiceClient = bookServiceClient;
    }


    private void makeHierarchy(List<CategoryHierarchyDto> hierarchy, CategoryNode parent) {
        if(hierarchy == null || hierarchy.isEmpty()){
            return;
        }
        for(CategoryHierarchyDto dto : hierarchy){
            CategoryNode currentNode = new CategoryNode(dto.getId(), dto.getName());
            if (parent != null) {
                currentNode.setParent(parent);
            }
            categoryMap.put(dto.getId(), currentNode);

            // 자식 노드들에 대해 재귀적으로 호출
            makeHierarchy(dto.getChildren(), currentNode);
        }
    }

    public List<Long> getAncestorCategoryIds(Long categoryId) {
        List<Long> ancestorIds = new ArrayList<>();
        CategoryNode node = categoryMap.get(categoryId);

        while (node != null) {
            ancestorIds.add(node.getId());
            node = node.getParent();
        }

        return ancestorIds;
    }

    @Getter
    private static class CategoryNode {
        private Long id;
        private String name;
        private CategoryNode parent;

        public CategoryNode(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public void setParent(CategoryNode parent) {
            this.parent = parent;
        }
    }
}
