package shop.wannab.couponservice.domain.category.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import shop.wannab.couponservice.client.BookServiceClient;
import shop.wannab.couponservice.domain.category.category.dto.CategoryHierarchyDto;

@Service
public class CategoryService {
    private final BookServiceClient bookServiceClient;

    public CategoryService(BookServiceClient bookServiceClient) {
        this.bookServiceClient = bookServiceClient;
    }

    public List<CategoryHierarchyDto> getCategoryHierarchy() {
        return bookServiceClient.getCategoryHierarchy();
    }

    public List<Long> getAncestorCategoryIds(Long targetCategoryId) {
        List<CategoryHierarchyDto> wholeHierarchy = bookServiceClient.getCategoryHierarchy();
        Map<Long, CategoryNode> tempCategoryMap = new HashMap<>();
        buildTemporaryMap(wholeHierarchy, null, tempCategoryMap);
        List<Long> ancestorIds = new ArrayList<>();
        CategoryNode node = tempCategoryMap.get(targetCategoryId);

        while (node != null) {
            ancestorIds.add(node.getId());
            node = node.getParent();
        }

        return ancestorIds;
    }

    private void buildTemporaryMap(List<CategoryHierarchyDto> hierarchy, CategoryNode parent,
                                   Map<Long, CategoryNode> map) {
        if (hierarchy == null || hierarchy.isEmpty()) {
            return;
        }
        for (CategoryHierarchyDto dto : hierarchy) {
            CategoryNode currentNode = new CategoryNode(dto.getId(), dto.getName());
            if (parent != null) {
                currentNode.setParent(parent);
            }
            map.put(dto.getId(), currentNode);

            buildTemporaryMap(dto.getChildren(), currentNode, map);
        }
    }

    @Getter
    @Setter
    private static class CategoryNode {
        private Long id;
        private String name;
        private CategoryNode parent;

        public CategoryNode(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
