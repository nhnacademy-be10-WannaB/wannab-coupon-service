package shop.wannab.couponservice.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import shop.wannab.couponservice.domain.Coupon;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponStatus;
import shop.wannab.couponservice.domain.enums.PolicyRule;
import shop.wannab.couponservice.repository.CouponPolicyRepository;
import shop.wannab.couponservice.repository.CouponRepository;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final RestTemplate restTemplate;

    public CouponService(CouponRepository couponRepository, CouponPolicyRepository couponPolicyRepository, RestTemplate restTemplate) {
        this.couponRepository = couponRepository;
        this.couponPolicyRepository = couponPolicyRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public void issueWelcomeCouponForNewUser(Long userId) {
        CouponPolicy welcomePolicy = couponPolicyRepository.findByPolicyRule(PolicyRule.WELCOME);

        if(welcomePolicy == null) {
            throw new IllegalArgumentException("웰컴 쿠폰이 없습니다.");
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, welcomePolicy)) {
            throw new IllegalArgumentException("이미 웰컴 쿠폰을 발급받았습니다.");
        }

        saveNewCoupon(userId, welcomePolicy, "WC");
    }

    @Transactional
    public void issueGeneralCoupon(Long userId, Long couponPolicyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId).orElse(null);

        if (couponPolicy == null) {
            throw new IllegalArgumentException("해당 쿠폰이 없습니다.");
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, couponPolicy)) {
            throw new IllegalArgumentException("이미 해당 쿠폰을 발급받았습니다.");
        }

        saveNewCoupon(userId,couponPolicy,"CST");
    }

    @Transactional
    public void issueBirthdayCoupon(int month) {
        System.out.println("생일 쿠폰 발급 로직 시작 (월: " + month + ")");

        CouponPolicy birthdayPolicy = couponPolicyRepository.findByPolicyRule(PolicyRule.BIRTHDAY);
        if (birthdayPolicy == null) {
            throw new IllegalArgumentException("해당 쿠폰이 없습니다.");
        }
        //향후 경로 맞출것
        String userServiceBaseUrl = "http://localhost:8082";

        String requestUrl = UriComponentsBuilder.fromHttpUrl(userServiceBaseUrl)
                .path("/api/users/birthdays")
                .queryParam("month", month)
                .toUriString();

        List<Long> birthdayUserIds;

        try {
            Long[] userIdsArray = restTemplate.getForObject(requestUrl, Long[].class);
            if (userIdsArray == null) {
                birthdayUserIds = List.of();
            } else {
                birthdayUserIds = Arrays.asList(userIdsArray);
            }
        } catch (Exception e) {
            birthdayUserIds = List.of();
        }

        for (Long userId : birthdayUserIds) {
            try {
                saveNewCoupon(userId, birthdayPolicy,"BD");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("생일 쿠폰 발급 실패");
            }
        }
    }

    private void saveNewCoupon(Long userId, CouponPolicy couponPolicy,String prefix) {
        String couponCode = String.format("%s%s-%s",prefix,LocalDate.now().toString().replace("-",""),
                UUID.randomUUID().toString().substring(0,20).replace("-","").toUpperCase());

        Coupon coupon = Coupon.builder()
                .userId(userId)
                .couponPolicy(couponPolicy)
                .couponCode(couponCode)
                .issuedAt(LocalDate.now())
                .startDate(LocalDate.now())
                .endDate(couponPolicy.getFixedEndDate())
                .status(CouponStatus.NOT_USED)
                .build();

        couponRepository.save(coupon);
    }
}
