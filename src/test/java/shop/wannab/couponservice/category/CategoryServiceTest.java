package shop.wannab.couponservice.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.couponservice.category.dto.CategoryHierarchyDto;
import shop.wannab.couponservice.client.BookServiceClient;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private BookServiceClient bookServiceClient;

    @InjectMocks
    private CategoryService categoryService;

    private List<CategoryHierarchyDto> categoryHierarchy;

    @BeforeEach
    void setUp() {
        CategoryHierarchyDto child2 = new CategoryHierarchyDto(3L, "소설", null);
        CategoryHierarchyDto parent1 = new CategoryHierarchyDto(2L, "국내도서", List.of(child2));
        CategoryHierarchyDto child3 = new CategoryHierarchyDto(4L, "외국도서", null);
        CategoryHierarchyDto root = new CategoryHierarchyDto(1L, "도서", List.of(parent1, child3));
        categoryHierarchy = List.of(root);
    }

    @Test
    @DisplayName("카테고리 계층 구조를 그대로 반환한다")
    void getCategoryHierarchy_Success() {
        when(bookServiceClient.getCategoryHierarchy()).thenReturn(categoryHierarchy);

        List<CategoryHierarchyDto> result = categoryService.getCategoryHierarchy();

        assertThat(result).isEqualTo(categoryHierarchy);
    }

    @Test
    @DisplayName("특정 카테고리의 상위 카테고리 ID 목록을 정확히 반환한다")
    void getAncestorCategoryIds_Success() {
        Long targetCategoryId = 3L;
        when(bookServiceClient.getCategoryHierarchy()).thenReturn(categoryHierarchy);

        List<Long> ancestorIds = categoryService.getAncestorCategoryIds(targetCategoryId);

        assertThat(ancestorIds).containsExactlyInAnyOrder(3L, 2L, 1L);
    }

    @Test
    @DisplayName("최상위 카테고리의 경우 자기 자신만 반환한다")
    void getAncestorCategoryIds_ForRoot_Success() {
        Long targetCategoryId = 1L;
        when(bookServiceClient.getCategoryHierarchy()).thenReturn(categoryHierarchy);

        List<Long> ancestorIds = categoryService.getAncestorCategoryIds(targetCategoryId);

        assertThat(ancestorIds).containsExactly(1L);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리의 경우 빈 목록을 반환한다")
    void getAncestorCategoryIds_ForNonExistent_ReturnsEmpty() {
        Long targetCategoryId = 99L;
        when(bookServiceClient.getCategoryHierarchy()).thenReturn(categoryHierarchy);

        List<Long> ancestorIds = categoryService.getAncestorCategoryIds(targetCategoryId);

        assertThat(ancestorIds).isEmpty();
    }
}
