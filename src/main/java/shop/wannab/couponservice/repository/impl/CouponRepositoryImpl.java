package shop.wannab.couponservice.repository.impl;

import static shop.wannab.couponservice.domain.couponpolicy.QCouponPolicy.couponPolicy;
import static shop.wannab.couponservice.domain.couponpolicy.QPolicyTargetCategory.policyTargetCategory;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.PolicyStatus;
import shop.wannab.couponservice.repository.CouponRepositoryCustom;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;


//    @Override
//    public List<ApplicableCouponInfo> findApplicableCouponsForOrder(Long userId, Map<Long, Long> bookIdToCategoryIdMap) {
//        List<ApplicableCouponInfo> applicableCoupons = new ArrayList<>();
//
//
//        //커스텀,웰컴,생일 쿠폰 가져오기
//        List<Coupon> orderCoupons = queryFactory
//                .selectFrom(coupon)
//                .join(coupon.couponPolicy, couponPolicy).fetchJoin()
//                .where(
//                        coupon.userId.eq(userId),
//                        coupon.status.eq(CouponStatus.NOT_USED),
//                        couponPolicy.couponType.in(CouponType.CUSTOM, CouponType.WELCOME, CouponType.BIRTHDAY)
//                )
//                .fetch();
//
//        orderCoupons.forEach(c -> applicableCoupons.add(new ApplicableCouponInfo(c, null)));
//
//
//        if (bookIdToCategoryIdMap == null || bookIdToCategoryIdMap.isEmpty()) {
//            return applicableCoupons;
//        }
//        Set<Long> bookIdsInCart = bookIdToCategoryIdMap.keySet();
//
//
//        List<Tuple> bookCouponResults = queryFactory
//                .select(coupon, policyTargetBook.bookId)
//                .from(coupon)
//                .join(coupon.couponPolicy, couponPolicy)
//                .join(policyTargetBook).on(policyTargetBook.couponPolicy.eq(couponPolicy))
//                .where(
//                        coupon.userId.eq(userId),
//                        coupon.status.eq(CouponStatus.NOT_USED),
//                        couponPolicy.couponType.eq(CouponType.BOOK),
//                        policyTargetBook.bookId.in(bookIdsInCart)
//                )
//                .fetch();
//
//        bookCouponResults.forEach(tuple -> {
//            Coupon c = tuple.get(coupon);
//            Long targetBookId = tuple.get(policyTargetBook.bookId);
//            applicableCoupons.add(new ApplicableCouponInfo(c, targetBookId));
//        });
//
//
//
//        Set<Long> categoryIdsInCart = bookIdToCategoryIdMap.values().stream().collect(Collectors.toSet());
//        List<Tuple> categoryCouponResults = queryFactory
//                .select(coupon, policyTargetCategory.categoryId)
//                .from(coupon)
//                .join(coupon.couponPolicy, couponPolicy)
//                .join(policyTargetCategory).on(policyTargetCategory.couponPolicy.eq(couponPolicy))
//                .where(
//                        coupon.userId.eq(userId),
//                        coupon.status.eq(CouponStatus.NOT_USED),
//                        couponPolicy.couponType.eq(CouponType.CATEGORY),
//                        policyTargetCategory.categoryId.in(categoryIdsInCart)
//                )
//                .fetch();
//
//        categoryCouponResults.forEach(tuple -> {
//            Coupon categoryCoupon = tuple.get(coupon);
//            Long targetCategoryId = tuple.get(policyTargetCategory.categoryId);
//
//            bookIdToCategoryIdMap.entrySet().stream()
//                    .filter(entry -> entry.getValue().equals(targetCategoryId))
//                    .forEach(entry -> {
//                        Long targetBookId = entry.getKey();
//                        applicableCoupons.add(new ApplicableCouponInfo(categoryCoupon, targetBookId));
//                    });
//        });
//
//        return applicableCoupons;
//    }

    public List<CouponPolicy> findActiveCouponPolicies(List<Long> ancestorCategoryIds) {
        return queryFactory
                .selectFrom(couponPolicy)
                .join(policyTargetCategory).on(policyTargetCategory.couponPolicy.eq(couponPolicy))
                .where(
                        policyTargetCategory.categoryId.in(ancestorCategoryIds),
                        couponPolicy.policyStatus.eq(PolicyStatus.ACTIVE)
                )
                .fetch();
    }
}

