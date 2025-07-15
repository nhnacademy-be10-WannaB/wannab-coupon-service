package shop.wannab.couponservice.couponpolicy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.couponservice.category.CategoryService;
import shop.wannab.couponservice.coupon.service.CouponService;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.service.CouponPolicyService;

@ActiveProfiles("ci")
@AutoConfigureRestDocs
@DisplayName("CouponPolicy Controller 단위 테스트")
@WebMvcTest(CouponPolicyController.class)
public class CouponPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponPolicyService couponPolicyService;

    @MockBean
    private CouponService couponService;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("쿠폰 정책 생성 API - 성공")
    void createCouponPolicy_Success() throws Exception {
        CreateCouponPolicyDto requestDto = new CreateCouponPolicyDto();
        requestDto.setName("유효한 쿠폰 정책");
        requestDto.setCouponType("NORMAL");
        requestDto.setDiscountType("FIXED");
        requestDto.setDiscountValue(1000);
        doNothing().when(couponPolicyService).createCouponPolicy(any(CreateCouponPolicyDto.class));

        mockMvc.perform(post("/api/admin/coupon_policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andDo(document("coupon-policy-create",
                relaxedRequestFields(
                    fieldWithPath("couponType").description("쿠폰 타입 (NORMAL, BIRTHDAY, BOOK, CATEGORY)"),
                    fieldWithPath("targetBookId").description("적용 도서 ID").optional(),
                    fieldWithPath("targetCategoryId").description("적용 카테고리 ID").optional(),
                    fieldWithPath("name").description("쿠폰 정책 이름"),
                    fieldWithPath("discountType").description("할인 타입 (FIXED, PERCENTAGE)"),
                    fieldWithPath("minPurchase").description("최소 구매 금액").optional(),
                    fieldWithPath("discountValue").description("할인 값"),
                    fieldWithPath("maxDiscount").description("최대 할인 금액").optional(),
                    fieldWithPath("validDays").description("유효 기간(일)").optional(),
                    fieldWithPath("startDate").description("시작일").optional(),
                    fieldWithPath("endDate").description("종료일").optional(),
                    fieldWithPath("birthday").description("생일 쿠폰 여부").optional(),
                    fieldWithPath("welcome").description("웰컴 쿠폰 여부").optional()
                )
            ));

        verify(couponPolicyService).createCouponPolicy(any(CreateCouponPolicyDto.class));
    }

    @Test
    @DisplayName("전체 쿠폰 정책 조회 API - 성공")
    void getAllCouponPolicies_Success() throws Exception {
        given(categoryService.getCategoryHierarchy()).willReturn(Collections.emptyList());
        given(couponPolicyService.getCouponPolicies()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/coupon_policies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categoryHierarchy").isArray())
            .andExpect(jsonPath("$.couponPolicies").isArray())
            .andDo(document("get-all-coupon-policies",
                relaxedResponseFields(
                    fieldWithPath("categoryHierarchy").description("카테고리 계층 구조"),
                    fieldWithPath("couponPolicies").description("쿠폰 정책 목록")
                )
            ));
    }

    @Test
    @DisplayName("쿠폰 정책 삭제 API - 성공")
    void deleteCouponPolicy_Success() throws Exception {
        Long policyId = 1L;
        doNothing().when(couponPolicyService).deleteCouponPolicyById(anyLong());

        mockMvc.perform(delete("/api/admin/coupon_policies/{policyId}", policyId))
            .andExpect(status().isOk())
            .andDo(document("delete-coupon-policy",
                pathParameters(
                    parameterWithName("policyId").description("삭제할 쿠폰 정책 ID")
                )
            ));

        verify(couponPolicyService).deleteCouponPolicyById(policyId);
    }

    @Test
    @DisplayName("생일 쿠폰 발급 요청 성공")
    void issueBirthdayCoupon_Success() throws Exception {
        doNothing().when(couponService).issueBirthdayCoupon(anyInt());

        mockMvc.perform(post("/api/admin/coupon_policies/issue-birthday")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(couponService, times(1)).issueBirthdayCoupon(anyInt());
    }
}
