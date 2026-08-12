package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.ReviewRequest;
import com.ecommerce.api.dto.response.PageResponse;
import com.ecommerce.api.dto.response.ReviewResponse;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.Review;
import com.ecommerce.api.enums.OrderStatus;
import com.ecommerce.api.enums.ProductStatus;
import com.ecommerce.api.enums.ReviewEligibility;
import com.ecommerce.api.enums.ReviewStatus;
import com.ecommerce.api.exception.BusinessRuleException;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.ReviewMapper;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.ReviewRepository;
import com.ecommerce.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    // ============ READ ============

    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        // Cố ý không check product tồn tại: product lạ → trả page rỗng là hợp lý,
        // và tiết kiệm 1 query trên đường đọc nóng nhất của module này.
        Page<ReviewResponse> reviews = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.ACTIVE, pageable).map(reviewMapper::fromEntity);
        return PageResponse.from(reviews);

    }

    public PageResponse<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        Page<ReviewResponse> reviews = reviewRepository.findByUserIdAndStatus(userId, ReviewStatus.ACTIVE, pageable).map(reviewMapper::fromEntity);
        return PageResponse.from(reviews);
    }

    /**
     * Cho FE quyết định hiện/ẩn nút "Viết đánh giá" — tránh để user gõ xong mới ăn lỗi 403.
     */
    public boolean canReview(Long userId, Long productId) {
        if (!productRepository.existsByIdAndStatus(productId, ProductStatus.ACTIVE)) return false;
        return checkEligibility(userId, productId) == ReviewEligibility.OK;
    }

    // ============ WRITE ============

    @Transactional
    public ReviewResponse createReview(Long userId, Long productId, ReviewRequest request) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        switch (checkEligibility(userId, productId)) {
            case ALREADY_REVIEWED -> throw new DuplicateResource("You have already reviewed this product");
            case NOT_PURCHASED    -> throw new BusinessRuleException("You can only review products you have purchased");
            case OK               -> { /* đủ điều kiện — đi tiếp */ }
        }

        Review review = reviewMapper.fromRequestDto(request);
        review.setProduct(product);
        review.setUser(userRepository.getReferenceById(userId));
        reviewRepository.save(review);

        // Map sang DTO TRƯỚC recompute: clearAutomatically detach toàn bộ persistence context,
        // đọc review.user sau đó sẽ là LazyInitializationException.
        ReviewResponse reviewResponse = reviewMapper.fromEntity(review);
        productRepository.recomputeRating(productId);
        log.info("Review created: reviewId={}, productId={}, userId={}, rating={}",
                review.getId(), productId, userId, request.getRating());
        return reviewResponse ;
    }

    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request) {
        boolean shouldRecompute = false;
        Review review = reviewRepository.findWithProductById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if(!review.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Review not found");

        if(review.getStatus().equals(ReviewStatus.DELETED)) throw new BusinessRuleException("Review is deleted");

        if(!review.getRating().equals(request.getRating())) {
            review.setRating(request.getRating());
            shouldRecompute = true;
        }
        review.setComment(request.getComment());


        ReviewResponse response = reviewMapper.fromEntity(review);
        if(shouldRecompute) productRepository.recomputeRating(review.getProduct().getId());
        log.info("Review updated: reviewId={}, userId={}, ratingChanged={}", reviewId, userId, shouldRecompute);
        return response;
    }

    /**
     * Xoá mềm — chỉ ADMIN (chặn ở controller bằng @PreAuthorize).
     * Chủ review KHÔNG được xoá: nếu được, seller sẽ ép/dụ khách gỡ review xấu.
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findWithProductById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Idempotent: gỡ 2 lần không lỗi, và không chạy thừa 1 câu recompute
        if (review.getStatus() == ReviewStatus.DELETED) return;

        // Đọc id TRƯỚC recompute: clearAutomatically detach entity, đọc sau là rủi ro không cần thiết
        Long productId = review.getProduct().getId();
        Long authorId  = review.getUser().getId();

        review.setStatus(ReviewStatus.DELETED);
        productRepository.recomputeRating(productId);

        // warn (không phải info): gỡ nội dung là hành động kiểm duyệt, cần nổi bật trong log
        // để truy vết được khi có khiếu nại — cột updated_by chỉ nói AI, log nói KHI NÀO + CÁI GÌ.
        log.warn("Review soft-deleted by admin: reviewId={}, productId={}, authorId={}", reviewId, productId, authorId);
    }

    /**
     * Một bộ luật dùng chung cho createReview (cần biết lý do) và canReview (chỉ cần có/không).
     * Trả kết quả thay vì ném exception → caller nào cần exception thì tự dịch, caller nào
     * cần boolean thì so sánh — không ai phải bắt exception để làm luồng điều khiển.
     * <p>
     * KHÔNG kiểm tra product ở đây: createReview cần chính object Product (để gắn quan hệ),
     * còn canReview chỉ cần biết nó tồn tại — hai nhu cầu khác nhau nên để caller tự lo.
     */
    private ReviewEligibility checkEligibility(Long userId, Long productId) {
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) return ReviewEligibility.ALREADY_REVIEWED;
        if (!orderRepository.verifiedPurchase(userId, productId, OrderStatus.DELIVERED.name())) return ReviewEligibility.NOT_PURCHASED;
        return ReviewEligibility.OK;
    }

    // ============================================================================
    // GHI CHÚ QUAN TRỌNG — vì sao recompute phải gọi qua productRepository
    //
    // TUYỆT ĐỐI KHÔNG làm kiểu này:
    //     product.setReviewCount(product.getReviewCount() + 1);
    //     product.setAverageRating(...);          // dirty checking lo phần còn lại
    //
    // Đó là read-modify-write ở tầng JVM → có khoảng hở cho request khác chen vào:
    //   - Không @Version  → lost update âm thầm, không exception nào báo
    //   - Có @Version     → 2 người review cùng lúc thì 1 người ăn 409 dù chẳng làm gì sai,
    //                       và Product.version bị đẩy lên bởi review → SELLER đang sửa giá
    //                       sản phẩm sẽ bị 409 oan (đụng Cụm 2.3b ở ROADMAP 4.0)
    //
    // Cách đúng: đẩy phép tính XUỐNG DB bằng 1 câu UPDATE atomic (ProductRepository.recomputeRating).
    // Đọc + tính + ghi nằm gọn trong 1 statement → không có khoảng hở, không cần lock,
    // không đụng tới version của Product. Cùng ý tưởng với Sequelize.increment / Mongo $inc.
    // ============================================================================
}
