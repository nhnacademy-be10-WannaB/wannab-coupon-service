package shop.wannab.couponservice.couponpolicy.service.couponcreator;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;

class CouponPolicyCreatorFactoryTest {

    private CouponPolicyCreatorFactory factory;
    private CouponPolicyCreator normalCreator;
    private CouponPolicyCreator bookCreator;
    private CouponPolicyCreator categoryCreator;

    @BeforeEach
    void setUp() {
        normalCreator = mock(CouponPolicyCreator.class);
        when(normalCreator.supports("NORMAL")).thenReturn(true);

        bookCreator = mock(CouponPolicyCreator.class);
        when(bookCreator.supports("BOOK")).thenReturn(true);

        categoryCreator = mock(CouponPolicyCreator.class);
        when(categoryCreator.supports("CATEGORY")).thenReturn(true);

        factory = new CouponPolicyCreatorFactory(List.of(normalCreator, bookCreator, categoryCreator));
    }

    @Test
    @DisplayName("요청한 타입의 Creator를 찾아 반환")
    void findCreator(){
        CouponPolicyCreator foundNormalCreator = factory.findCreator("NORMAL");
        assertThat(foundNormalCreator).isEqualTo(normalCreator);

        CouponPolicyCreator foundBookCreator = factory.findCreator("BOOK");
        assertThat(foundBookCreator).isEqualTo(bookCreator);

        CouponPolicyCreator foundCategoryCreator = factory.findCreator("CATEGORY");
        assertThat(foundCategoryCreator).isEqualTo(categoryCreator);
    }

    @Test
    @DisplayName("지원하지 않는 타입일 경우 CouponPolicyException을 던진다")
    void findCreator_WhenNoCreatorExists_ShouldThrowException() {
        assertThrows(CouponPolicyException.class, () -> {
            factory.findCreator("UNKNOWN_TYPE");
        });
    }
}
