package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.ReviewRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.PageResponse;
import com.ecommerce.api.dto.response.ReviewResponse;
import com.ecommerce.api.security.CustomUserDetails;
import com.ecommerce.api.services.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Review API — cố ý KHÔNG đặt prefix hẹp ở class, vì 5 endpoint nằm ở 2 nhánh URL:
 *   - nhánh /products/{productId}/reviews : list + create (review luôn thuộc về 1 product)
 *   - nhánh /reviews/{reviewId}           : update + delete (reviewId đã unique toàn hệ thống,
 *                                            bắt client gửi kèm productId là thừa và còn đẻ
 *                                            thêm nhánh validate "2 id không khớp nhau")
 *
 * Lưu ý security: SecurityConfig permitAll cho GET /api/products/** → endpoint list review
 * công khai sẵn. Nhưng can-review cũng nằm dưới prefix đó nên phải khai .authenticated()
 * RIÊNG và đặt TRƯỚC dòng permitAll (Spring Security lấy luật khớp đầu tiên), nếu không
 * request ẩn danh sẽ lọt vào và @AuthenticationPrincipal trả null → NPE.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<BaseResponse<PageResponse<ReviewResponse>>> getProductReviews(@PathVariable("productId") Long productId, Pageable pageable) {
        return ResponseEntity.ok(BaseResponse.success(reviewService.getProductReviews(productId,pageable)));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<BaseResponse<ReviewResponse>> createProductReview(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(reviewService.createReview(user.getId(), productId, request)));
    }

    @GetMapping("/reviews/me")
    public ResponseEntity<BaseResponse<PageResponse<ReviewResponse>>> getMyReviews(Pageable page, @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(BaseResponse.success(reviewService.getMyReviews(userDetails.getId(), page)));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<ReviewResponse>> updateReview(
                                                        @PathVariable("reviewId") Long reviewId,
                                                        @Valid @RequestBody ReviewRequest request,
                                                        @AuthenticationPrincipal CustomUserDetails user
    )  {
        return ResponseEntity.ok(BaseResponse.success(reviewService.updateReview(user.getId(), reviewId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> deleteReview(@PathVariable("reviewId") Long reviewId){
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(BaseResponse.success(null, "Delete review success!"));
    }

    @GetMapping("/products/{productId}/reviews/can-review")
    public ResponseEntity<BaseResponse<Boolean>> canReview(@PathVariable("productId") Long productId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(BaseResponse.success(reviewService.canReview( user.getId(), productId)));
    }

}
