package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetBookRepository;


@ExtendWith(MockitoExtension.class)
public class BookCouponPolicyCreatorTest {

    @Mock
    private CouponPolicyRepository couponPolicyRepository;
    @Mock
    private PolicyTargetBookRepository policyTargetBookRepository;

    @InjectMocks
    private BookCouponPolicyCreator bookCouponPolicyCreator;

    @Test
    @DisplayName("지원하는 타입(BOOK)이 들어오면 True를 반환한다")
    void supports_WithBookType_ShouldReturnTrue() {
        boolean result = bookCouponPolicyCreator.supports("BOOK");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("지원하지 않는 타입이 들어오면 false를 반환한다")
    void supports_WithOtherType_ShouldReturnFalse() {
        boolean result = bookCouponPolicyCreator.supports("NORMAL");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("도서 쿠폰 생성에 성공한다")
    void createCouponPolicy_Success() {
        long bookId = 101L;
        CreateCouponPolicyDto request = createBookCouponRequest(bookId);

        when(policyTargetBookRepository.findByBookIdAndCouponPolicy_PolicyStatus(bookId, PolicyStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(couponPolicyRepository.save(any(CouponPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookCouponPolicyCreator.createCouponPolicy(request);

        ArgumentCaptor<PolicyTargetBook> targetBookCaptor = ArgumentCaptor.forClass(PolicyTargetBook.class);
        verify(policyTargetBookRepository).save(targetBookCaptor.capture());

        PolicyTargetBook savedTargetBook = targetBookCaptor.getValue();
        assertThat(savedTargetBook.getBookId()).isEqualTo(bookId);
        assertThat(savedTargetBook.getCouponPolicy()).isNotNull();
        assertThat(savedTargetBook.getCouponPolicy().getCouponType()).isEqualTo(CouponType.BOOK);
    }

    @Test
    @DisplayName("유효하지 않은 도서 ID(0 이하)로 생성 요청 시 INVALID_BOOK_ID 예외를 던진다")
    void createCouponPolicy_WithInvalidBookId_ShouldThrowException() {
        CreateCouponPolicyDto request = createBookCouponRequest(0L);
        CouponPolicyException exception = assertThrows(CouponPolicyException.class, () -> {
            bookCouponPolicyCreator.createCouponPolicy(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(CouponPolicyErrorCode.INVALID_BOOK_ID);
        verify(couponPolicyRepository, never()).save(any());
        verify(policyTargetBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 해당 도서의 쿠폰 정책이 존재할 경우 BOOK_POLICY_ALREADY_EXISTS 예외를 던진다")
    void createCouponPolicy_WhenPolicyForBookAlreadyExists_ShouldThrowException() {
        long bookId = 101L;
        CreateCouponPolicyDto request = createBookCouponRequest(bookId);

        when(policyTargetBookRepository.findByBookIdAndCouponPolicy_PolicyStatus(bookId, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(new PolicyTargetBook()));

        CouponPolicyException exception = assertThrows(CouponPolicyException.class, () -> {
            bookCouponPolicyCreator.createCouponPolicy(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(CouponPolicyErrorCode.BOOK_POLICY_ALREADY_EXISTS);
        verify(couponPolicyRepository, never()).save(any());
    }

    private CreateCouponPolicyDto createBookCouponRequest(long bookId) {
        CreateCouponPolicyDto request = new CreateCouponPolicyDto();
        request.setName("JPA 프로그래밍 도서 쿠폰");
        request.setDiscountType("FIXED");
        request.setDiscountValue(3000);
        request.setTargetBookId(bookId);
        return request;
    }
}