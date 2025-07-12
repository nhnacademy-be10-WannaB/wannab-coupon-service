package shop.wannab.couponservice.couponpolicy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.couponservice.category.CategoryService;
import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;
import shop.wannab.couponservice.couponpolicy.service.CouponPolicyService;


@WebMvcTest(CouponPolicyController.class)
public class CouponPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponPolicyService couponPolicyService;

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

        mockMvc.perform(post("/api/admin/coupon_policies") // POST 요청
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(couponPolicyService).createCouponPolicy(any(CreateCouponPolicyDto.class));
    }

    @Test
    @DisplayName("전체 쿠폰 정책 조회 API - 성공")
    void getAllCouponPolicies_Success() throws Exception {
        given(categoryService.getCategoryHierarchy()).willReturn(Collections.emptyList());
        given(couponPolicyService.getCouponPolicies()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/coupon_policies")) // GET 요청
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryHierarchy").isArray())
                .andExpect(jsonPath("$.couponPolicies").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("쿠폰 정책 삭제 API - 성공")
    void deleteCouponPolicy_Success() throws Exception {
        Long policyId = 1L;
        doNothing().when(couponPolicyService).deleteCouponPolicyById(anyLong());

        mockMvc.perform(delete("/api/admin/coupon_policies/{policyId}", policyId))
                .andExpect(status().isOk())
                .andDo(print());

        verify(couponPolicyService).deleteCouponPolicyById(policyId);
    }
}
