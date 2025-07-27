package shop.wannab.couponservice.coupon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponsDto;
import shop.wannab.couponservice.coupon.dto.BookCouponDto;
import shop.wannab.couponservice.coupon.dto.CouponResponseToUserDto;
import shop.wannab.couponservice.coupon.dto.CouponUsageRequestDto;
import shop.wannab.couponservice.coupon.dto.OrderCouponDto;
import shop.wannab.couponservice.coupon.dto.OrderCouponsRequestDto;
import shop.wannab.couponservice.coupon.dto.PageResponseDto;
import shop.wannab.couponservice.coupon.dto.TryApplyCouponsRequestDto;
import shop.wannab.couponservice.coupon.dto.TryApplyCouponsResponseDto;
import shop.wannab.couponservice.coupon.entity.Coupon;
import shop.wannab.couponservice.coupon.entity.CouponStatus;
import shop.wannab.couponservice.coupon.service.CouponService;
import shop.wannab.couponservice.couponpolicy.dto.IssuableCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.service.CouponPolicyService;

@ActiveProfiles("ci")
@DisplayName("Coupon Controller 단위 테스트")
@WebMvcTest(CouponController.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class CouponControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CouponService couponService;
    @MockBean private CouponPolicyService couponPolicyService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }
    @Test
    @DisplayName("사용 가능한 쿠폰 조회 API - 성공")
    void getApplicableCoupons_Success() throws Exception {
        Long userId = 1L;
        Map<Long, List<BookCouponDto>> bookCouponMap = Collections.singletonMap(1L, List.of(
                new BookCouponDto(1L, "테스트 도서 쿠폰", 500, DiscountType.FIXED)
        ));
        List<OrderCouponDto> orderCouponDtos = List.of(new OrderCouponDto(2L, "테스트 주문 쿠폰", 1000, DiscountType.FIXED));
        ApplicableCouponsDto responseDto = new ApplicableCouponsDto(bookCouponMap, orderCouponDtos);

        given(couponService.getUserApplicableCoupons(anyLong(), any())).willReturn(responseDto);


        OrderCouponsRequestDto requestDto = new OrderCouponsRequestDto();

        mockMvc.perform(post("/api/coupons/order")
                        .header("X-USER-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andDo(document("coupon/get-applicable-coupons",
                        requestHeaders(headerWithName("X-USER-ID").description("사용자 ID")),
                        requestFields(
                                fieldWithPath("bookIds").description("적용 가능한 쿠폰을 조회할 도서 ID 목록. (null일 수 있음)").optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("itemCoupons").description("도서별 쿠폰 목록"),
                                fieldWithPath("itemCoupons.*[].couponId").description("쿠폰 ID"),
                                fieldWithPath("itemCoupons.*[].couponName").description("쿠폰 이름"),
                                fieldWithPath("itemCoupons.*[].discountValue").description("할인 값"),
                                fieldWithPath("itemCoupons.*[].discountType").description("할인 타입 (PERCENT, FIXED)"),
                                fieldWithPath("orderCoupons[].couponId").description("주문 쿠폰 ID"),
                                fieldWithPath("orderCoupons[].couponName").description("주문 쿠폰 이름"),
                                fieldWithPath("orderCoupons[].discountValue").description("주문 쿠폰 할인 값"),
                                fieldWithPath("orderCoupons[].discountType").description("주문 쿠폰 할인 타입 (PERCENT, FIXED)")
                        )
                ));
    }

    @Test
    @DisplayName("쿠폰 적용 시도 API - 성공")
    void tryApplyCoupons_Success() throws Exception {
        Long userId = 1L;
        TryApplyCouponsRequestDto requestDto = new TryApplyCouponsRequestDto();
        requestDto.setCouponAndBookIds(Map.of(100L, 1L, 101L, 2L));

        List<TryApplyCouponsResponseDto> responseDto = List.of(
            new TryApplyCouponsResponseDto(1L, 1000, DiscountType.FIXED, 1L)
        );

        given(couponService.applyCoupons(anyLong(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/coupons/order/apply")
                .header("X-USER-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andDo(document("coupon/try-apply-coupons",
                requestHeaders(headerWithName("X-USER-ID").description("사용자 ID")),
                    requestFields(
                            fieldWithPath("couponAndBookIds").description("쿠폰 ID를 키로, 해당 쿠폰을 적용할 도서 ID를 값으로 하는 매핑. (예: {\"100\": 1, \"101\": 2})"),
                            fieldWithPath("couponAndBookIds.*").description("매핑된 도서 ID (Long 타입)")
                    ),
                    relaxedResponseFields(
                            fieldWithPath("[].couponId").description("적용된 쿠폰 ID"),
                            fieldWithPath("[].discountValue").description("할인 금액"),
                            fieldWithPath("[].discountType").description("할인 타입 (PERCENT, FIXED)"),
                            fieldWithPath("[].bookId").description("할인이 적용된 도서 ID")
                    )
            ));
    }

    @Test
    @DisplayName("웰컴 쿠폰 발급 API - 성공")
    void issueWelcomeCouponForNewUser_Success() throws Exception {
        Long userId = 1L;
        doNothing().when(couponService).issueWelcomeCouponForNewUser(anyLong());

        mockMvc.perform(post("/api/coupons/issue/welcome?userId={userId}", userId))
                .andExpect(status().isOk())
                .andDo(document("coupon/issue-welcome-coupon",
                        queryParameters(parameterWithName("userId").description("신규 사용자 ID"))
                ));

        verify(couponService).issueWelcomeCouponForNewUser(userId);
    }

    @Test
    @DisplayName("관리자: 특정 월 생일 쿠폰 발급")
    void issueBirthdayCouponsManually_Success() throws Exception {
        int month = 7;
        doNothing().when(couponService).issueBirthdayCoupon(anyInt());

        mockMvc.perform(post("/api/coupons/issue/birthday?month={month}", month))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("coupon/issue-birthday-coupon",
                        queryParameters(
                                parameterWithName("month").description("생일 쿠폰을 발급할 월")
                        )
                ));
    }

    @Test
    @DisplayName("커스텀 쿠폰 발급 API - 성공")
    void issueCustomCoupon_Success() throws Exception {
        Long userId = 1L;
        Long couponPolicyId = 1L;
        doNothing().when(couponService).issueGeneralCoupon(anyLong(), anyLong());

        mockMvc.perform(post("/api/coupons/issue/custom?couponPolicyId={couponPolicyId}", couponPolicyId)
                        .header("X-USER-ID", userId))
                .andExpect(status().isOk())
                .andDo(document("coupon/issue-custom-coupon",
                        requestHeaders(headerWithName("X-USER-ID").description("사용자 ID")),
                        queryParameters(parameterWithName("couponPolicyId").description("쿠폰 정책 ID"))
                ));

        verify(couponService).issueGeneralCoupon(userId, couponPolicyId);
    }

    @Test
    @DisplayName("발급 가능한 쿠폰 조회 API - 성공")
    void getIssuableCoupons_Success() throws Exception {
        Long bookId = 1L;
        CouponPolicy policy = CouponPolicy.builder()
            .couponPolicyId(1L)
            .couponPolicyName("도서 전용 쿠폰")
            .couponType(CouponType.BOOK)
            .discountType(DiscountType.FIXED)
            .discountValue(1000)
            .maxDiscount(1000)
            .minPurchase(0)
            .policyStatus(PolicyStatus.ACTIVE)
            .build();

        given(couponPolicyService.findIssuablePoliciesForBook(anyLong()))
            .willReturn(List.of(new IssuableCouponPolicyDto(policy)));

        mockMvc.perform(get("/api/coupons/issuable-coupons")
                .param("bookId", String.valueOf(bookId)))
            .andExpect(status().isOk())
            .andDo(document("coupon/get-issuable-coupons",
                queryParameters(parameterWithName("bookId").description("도서 ID")),
                relaxedResponseFields(
                    fieldWithPath("[].couponPolicyId").description("정책 ID"),
                    fieldWithPath("[].name").description("정책 이름"),
                    fieldWithPath("[].discountInfo").description("할인 정보")
                )
            ));
    }

    @Test
    @DisplayName("사용자 쿠폰 조회 API - 성공")
    void getCouponsForUser_Success() throws Exception {
        Long userId = 1L;

        CouponPolicy dummyPolicy = CouponPolicy.builder()
            .couponPolicyId(1L)
            .couponPolicyName("테스트 쿠폰")
            .couponType(CouponType.CUSTOM)
            .discountType(DiscountType.FIXED)
            .discountValue(1000)
            .maxDiscount(1000)
            .minPurchase(0)
            .policyStatus(PolicyStatus.ACTIVE)
            .build();

        Coupon dummyCoupon = Coupon.builder()
            .couponId(1L)
            .userId(userId)
            .couponPolicy(dummyPolicy)
            .couponCode("TESTCODE456")
            .issuedAt(LocalDate.now())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(7))
            .status(CouponStatus.NOT_USED)
            .build();

        CouponResponseToUserDto couponResponse = new CouponResponseToUserDto(dummyCoupon);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CouponResponseToUserDto> page = new PageImpl<>(Collections.singletonList(couponResponse), pageable, 1);
        PageResponseDto<CouponResponseToUserDto> pageResponse = new PageResponseDto<>(page);

        given(couponService.getUserCoupons(anyLong(), any())).willReturn(pageResponse);

        mockMvc.perform(get("/api/coupons/me")
                .header("X-USER-ID", userId)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "couponId,desc"))
            .andExpect(status().isOk())
            .andDo(document("coupon/get-user-coupons",
                requestHeaders(
                    headerWithName("X-USER-ID").description("사용자 ID")
                ),
                queryParameters(
                    parameterWithName("page").description("페이지 번호").optional(),
                    parameterWithName("size").description("페이지 크기").optional(),
                    parameterWithName("sort").description("정렬 기준").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("content[].couponName").description("쿠폰 이름"),
                    fieldWithPath("content[].discountInfo").description("할인 정보"),
                    fieldWithPath("content[].purchaseTerm").description("사용 조건"),
                    fieldWithPath("content[].period").description("사용 기간"),
                    fieldWithPath("content[].usageStatus").description("사용 상태"),
                    fieldWithPath("pageNumber").description("현재 페이지 번호"),
                    fieldWithPath("pageSize").description("페이지 크기"),
                    fieldWithPath("totalPages").description("전체 페이지 수"),
                    fieldWithPath("totalElements").description("전체 요소 수"),
                    fieldWithPath("last").description("마지막 페이지 여부")
                )
            ));
    }

    @Test
    @DisplayName("쿠폰 사용 처리 API - 성공")
    void processUsedCoupons_Success() throws Exception {
        Long userId = 1L;
        CouponUsageRequestDto requestDto = new CouponUsageRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setUserId(userId);
        requestDto.setUsedCoupons(List.of(new CouponUsageRequestDto.UsedCouponInfo(1L, 1L)));

        doNothing().when(couponService).processUsedCoupons(anyLong(), any());

        mockMvc.perform(post("/api/coupons/order/success")
                .header("X-USER-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andDo(document("coupon/process-used-coupons",
                requestHeaders(headerWithName("X-USER-ID").description("사용자 ID")),
                requestFields(
                    fieldWithPath("orderId").description("주문 ID"),
                    fieldWithPath("userId").description("사용자 ID"),
                    fieldWithPath("usedCoupons").description("사용된 쿠폰 목록"),
                    fieldWithPath("usedCoupons[].couponId").description("사용된 쿠폰 ID"),
                    fieldWithPath("usedCoupons[].bookId").description("쿠폰이 적용된 도서 ID")
                )
            ));
    }
}
