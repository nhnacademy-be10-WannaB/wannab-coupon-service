package shop.wannab.couponservice.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.couponservice.category.category.dto.CategoryHierarchyDto;

//TODO: api 경로,book-service url 경로 정리
@FeignClient(name = "book-service",url = "${feign.client.book-service.url}")
public interface BookServiceClient {
    @PostMapping("/api/books/names")
    Map<Long, String> getBookNames(@RequestBody List<Long> bookIds);

    // 여러 카테고리 ID를 받아 이름 목록을 반환
    @PostMapping("/api/books/categories/names")
    Map<Long, String> getCategoryNames(@RequestBody List<Long> categoryIds);

    @GetMapping("/api/categories/hierarchy")
    List<CategoryHierarchyDto> getCategoryHierarchy();
}
