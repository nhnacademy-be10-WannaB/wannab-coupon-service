package shop.wannab.couponservice.service;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public CouponService(CouponRepository couponRepository, CouponPolicyRepository couponPolicyRepository) {
        this.couponRepository = couponRepository;
        this.couponPolicyRepository = couponPolicyRepository;
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
