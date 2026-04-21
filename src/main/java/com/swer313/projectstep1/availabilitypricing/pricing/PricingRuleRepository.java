package com.swer313.projectstep1.availabilitypricing.pricing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByActiveTrue();

    @Query("""
        SELECT r FROM PricingRule r
        WHERE r.active     = true
          AND r.startDate <= :date
          AND r.endDate   >= :date
        """)
    Optional<PricingRule> findActiveRuleForDate(
            @Param("date") LocalDate date);

    @Query("""
        SELECT r FROM PricingRule r
        WHERE r.active     = true
          AND r.startDate <= :endDate
          AND r.endDate   >= :startDate
        ORDER BY r.startDate ASC
        """)
    List<PricingRule> findActiveRulesInRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);

    @Query("""
        SELECT r FROM PricingRule r
        WHERE r.active     = true
          AND r.endDate   >= :startDate
          AND r.startDate <= :endDate
          AND (:excludeId IS NULL OR r.id <> :excludeId)
        """)
    List<PricingRule> findOverlapping(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate,
            @Param("excludeId") Long excludeId);
    // أضف هدول في PricingRuleRepository
    Page<PricingRule> findAllByOrderByStartDateDesc(Pageable pageable);
    Page<PricingRule> findByActiveTrueOrderByStartDateDesc(Pageable pageable);
}
