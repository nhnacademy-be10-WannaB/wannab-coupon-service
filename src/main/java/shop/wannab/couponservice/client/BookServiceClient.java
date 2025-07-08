package shop.wannab.couponservice.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import shop.wannab.couponservice.category.dto.CategoryHierarchyDto;

@FeignClient(name = "gateway", url = "${gateway.api.url}", path = "/book-service", contextId = "bookServiceClient")
public interface BookServiceClient {
    @PostMapping("/api/books/names")
    Map<Long, String> getBookNames(@RequestBody List<Long> bookIds);

    // 여러 카테고리 ID를 받아 이름 목록을 반환
    @PostMapping("/api/categories/names")
    Map<Long, String> getCategoryNames(@RequestBody List<Long> categoryIds);

    @GetMapping("/api/categories/hierarchy")
    List<CategoryHierarchyDto> getCategoryHierarchy();

    @PostMapping("/api/categories")
    List<Long> getCategoryIds(@RequestBody List<Long> bookIds);

    @GetMapping("/api/category")
    Long getCategoryId(@RequestParam Long id);
}
