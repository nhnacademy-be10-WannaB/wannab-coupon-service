package shop.wannab.couponservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.domain.coupon.Coupon;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.PolicyStatus;
import shop.wannab.couponservice.repository.CouponPolicyRepository;
import shop.wannab.couponservice.repository.CouponRepository;
import shop.wannab.couponservice.repository.impl.CouponRepositoryImpl;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponRepositoryImpl couponRepositoryImpl;
    //private final UserServiceClient userServiceClient;
    public CouponService(CouponRepository couponRepository,
                         CouponPolicyRepository couponPolicyRepository,
                         CouponRepositoryImpl couponRepositoryImpl
                         ) {
        //UserServiceClient userServiceClient
        this.couponRepository = couponRepository;
        this.couponPolicyRepository = couponPolicyRepository;
        this.couponRepositoryImpl = couponRepositoryImpl;
        //this.userServiceClient = userServiceClient;
    }

    @Transactional
    public void issueWelcomeCouponForNewUser(Long userId) {
        CouponPolicy welcomePolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE);

        if (welcomePolicy == null) {
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

        saveNewCoupon(userId, couponPolicy, "CST");
    }

//    //TODO: FeignClient로 변환 후 처리
//    @Transactional
//    public void issueBirthdayCoupon(int month) {
//        System.out.println("생일 쿠폰 발급 로직 시작 (월: " + month + ")");
//
//        CouponPolicy birthdayPolicy = couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY);
//        if (birthdayPolicy == null) {
//            throw new IllegalArgumentException("해당 쿠폰이 없습니다.");
//        }
//
//        List<Long> birthdayUserIds;
//
//        try {
//            birthdayUserIds = userServiceClient.getBirthdayUserIds(month);
//        } catch (Exception e) {
//            birthdayUserIds = List.of();
//        }
//
//        for (Long userId : birthdayUserIds) {
//            try {
//                saveNewCoupon(userId, birthdayPolicy, "BD");
//            } catch (IllegalArgumentException e) {
//                throw new IllegalArgumentException("생일 쿠폰 발급 실패");
//            }
//        }
//    }

    private void saveNewCoupon(Long userId, CouponPolicy couponPolicy, String prefix) {
        Coupon createdCoupon = Coupon.createNewCoupon(userId, couponPolicy, prefix);
        couponRepository.save(createdCoupon);
    }
}
