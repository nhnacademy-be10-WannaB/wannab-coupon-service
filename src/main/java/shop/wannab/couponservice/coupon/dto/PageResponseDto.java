package shop.wannab.couponservice.coupon.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
public class PageResponseDto<T>{
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private boolean isLast;

    public PageResponseDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.isLast = page.isLast();
    }
}
