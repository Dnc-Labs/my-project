package com.ecommerce.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Wrapper response cho dữ liệu paginated.
 * <p>
 * Vì sao tự tạo thay vì trả Page<T> trực tiếp:
 *   - PageImpl là internal class của Spring Data, JSON structure không stable
 *     (Spring 3.x đã warn: "Serializing PageImpl instances as-is is not supported")
 *   - Tự sở hữu contract → kiểm soát field name + structure cho frontend
 *
 * @param <T> kiểu element trong content (thường là DTO response, KHÔNG phải entity)
 */
@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;
    private PageMetaData page;

    @Getter
    @Builder
    public static class PageMetaData {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(PageMetaData.builder()
                                      .first(page.isFirst())
                                      .last(page.isLast())
                                      .totalPages(page.getTotalPages())
                                      .totalElements(page.getTotalElements())
                                      .number(page.getNumber())
                                      .size(page.getSize())
                                      .build())
                .build();
    }
}
