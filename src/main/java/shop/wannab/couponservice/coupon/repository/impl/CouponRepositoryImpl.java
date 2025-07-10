package shop.wannab.couponservice.coupon.repository.impl;

import static shop.wannab.couponservice.coupon.entity.QCoupon.coupon;
import static shop.wannab.couponservice.couponpolicy.entity.QCouponPolicy.couponPolicy;
import static shop.wannab.couponservice.couponpolicy.entity.QPolicyTargetBook.policyTargetBook;
import static shop.wannab.couponservice.couponpolicy.entity.QPolicyTargetCategory.policyTargetCategory;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.coupon.entity.Coupon;
import shop.wannab.couponservice.coupon.entity.CouponStatus;
import shop.wannab.couponservice.coupon.repository.CouponRepositoryCustom;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;


    @Override
    public List<ApplicableCouponInfo> findApplicableCouponsForOrder(
            Long userId,
            Map<Long, Set<Long>> bookIdToCategoryIdsMap) {

        List<ApplicableCouponInfo> applicableCoupons = new ArrayList<>();

        List<Coupon> orderCoupons = queryFactory
                .selectFrom(coupon)
                .join(coupon.couponPolicy, couponPolicy).fetchJoin()
                .where(
                        coupon.userId.eq(userId),
                        coupon.status.eq(CouponStatus.NOT_USED),
                        couponPolicy.couponType.in(CouponType.CUSTOM, CouponType.WELCOME, CouponType.BIRTHDAY)
                )
                .fetch();
        orderCoupons.forEach(c -> applicableCoupons.add(new ApplicableCouponInfo(c, null)));

        if (bookIdToCategoryIdsMap == null || bookIdToCategoryIdsMap.isEmpty()) {
            return applicableCoupons;
        }

        Set<Long> bookIdsInCart = bookIdToCategoryIdsMap.keySet();
        List<Tuple> bookCouponResults = queryFactory
                .select(coupon, policyTargetBook.bookId)
                .from(coupon)
                .join(coupon.couponPolicy, couponPolicy)
                .join(policyTargetBook).on(policyTargetBook.couponPolicy.eq(couponPolicy))
                .where(
                        coupon.userId.eq(userId),
                        coupon.status.eq(CouponStatus.NOT_USED),
                        couponPolicy.couponType.eq(CouponType.BOOK),
                        policyTargetBook.bookId.in(bookIdsInCart)
                )
                .fetch();
        bookCouponResults.forEach(tuple -> {
            Coupon c = tuple.get(coupon);
            Long targetBookId = tuple.get(policyTargetBook.bookId);
            applicableCoupons.add(new ApplicableCouponInfo(c, targetBookId));
        });


        Set<Long> allCategoryIdsInCart = bookIdToCategoryIdsMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        List<Tuple> categoryCouponResults = queryFactory
                .select(coupon, policyTargetCategory.categoryId)
                .from(coupon)
                .join(coupon.couponPolicy, couponPolicy)
                .join(policyTargetCategory).on(policyTargetCategory.couponPolicy.eq(couponPolicy))
                .where(
                        coupon.userId.eq(userId),
                        coupon.status.eq(CouponStatus.NOT_USED),
                        couponPolicy.couponType.eq(CouponType.CATEGORY),
                        policyTargetCategory.categoryId.in(allCategoryIdsInCart)
                )
                .fetch();

        categoryCouponResults.forEach(tuple -> {
            Coupon categoryCoupon = tuple.get(coupon);
            Long targetCategoryId = tuple.get(policyTargetCategory.categoryId);

            bookIdToCategoryIdsMap.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(targetCategoryId))
                    .forEach(entry -> {
                        Long targetBookId = entry.getKey();
                        applicableCoupons.add(new ApplicableCouponInfo(categoryCoupon, targetBookId));
                    });
        });

        return applicableCoupons;
    }

    @Override
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

