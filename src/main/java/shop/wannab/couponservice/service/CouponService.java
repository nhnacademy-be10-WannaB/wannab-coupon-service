package shop.wannab.couponservice.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import shop.wannab.couponservice.domain.coupon.Coupon;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;
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
        CouponPolicy welcomePolicy = couponPolicyRepository.findByCouponType(CouponType.WELCOME);

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

        CouponPolicy birthdayPolicy = couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY);
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
        Coupon createdCoupon = Coupon.createNewCoupon(userId, couponPolicy, prefix);
        couponRepository.save(createdCoupon);
    }
}
