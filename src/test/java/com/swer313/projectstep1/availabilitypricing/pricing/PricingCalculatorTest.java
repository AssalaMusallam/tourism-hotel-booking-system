package com.swer313.projectstep1.availabilitypricing.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingCalculatorTest {

	@Mock
	private PricingRuleRepository pricingRuleRepository;

	@InjectMocks
	private PricingCalculator pricingCalculator;

	/**
	 * helper لتعيين قيم private fields الموجودة في PricingCalculator
	 * لأن هذه القيم عادة تأتي من @Value / application.properties
	 */
	private void setPrivateField(String name, Object value) throws Exception {
		Field f = PricingCalculator.class.getDeclaredField(name);
		f.setAccessible(true);
		f.set(pricingCalculator, value);
	}

	@BeforeEach
	void setup() throws Exception {
		// إعداد القيم الافتراضية قبل كل test
		setPrivateField("weekendMultiplier", BigDecimal.valueOf(1.25));
		setPrivateField("taxRate", BigDecimal.valueOf(0.16));
		setPrivateField("weekendDaysConfig", "FRIDAY,SATURDAY");
	}

	@Test
	void calculateBreakdown_noSeasonalRule_appliesBaseAndTax() {
		// Arrange
		// لا يوجد seasonal rules
		when(pricingRuleRepository.findActiveRulesInRange(any(), any()))
				.thenReturn(Collections.emptyList());

		BigDecimal basePrice = BigDecimal.valueOf(100);
		LocalDate in = LocalDate.of(2026, 6, 1);   // Monday
		LocalDate out = LocalDate.of(2026, 6, 3);  // ليلتان

		// Act
		PriceBreakdownDTO dto = pricingCalculator.calculateBreakdown(basePrice, in, out);

		// Assert
		assertEquals(2, dto.getNights());
		assertEquals(new BigDecimal("200.00"), dto.getSubtotal());
		assertEquals(new BigDecimal("0.16"), dto.getTaxRate());
		assertEquals(new BigDecimal("32.00"), dto.getTaxAmount());
		assertEquals(new BigDecimal("232.00"), dto.getTotalPrice());
		assertEquals(2, dto.getBreakdown().size());

		// التحقق من الليلة الأولى
		assertFalse(dto.getBreakdown().get(0).isWeekend());
		assertNull(dto.getBreakdown().get(0).getAppliedRuleName());
		assertEquals(new BigDecimal("100.00"), dto.getBreakdown().get(0).getNightTotal());
	}

	@Test
	void calculateBreakdown_seasonalRuleApplied_includesRuleNameAndMultiplier() {
		// Arrange
		PricingRule rule = new PricingRule();
		rule.setName("Promo");
		rule.setStartDate(LocalDate.of(2026, 6, 2));
		rule.setEndDate(LocalDate.of(2026, 6, 2));
		rule.setPriceMultiplier(BigDecimal.valueOf(1.5));
		rule.setActive(true);

		when(pricingRuleRepository.findActiveRulesInRange(any(), any()))
				.thenReturn(Collections.singletonList(rule));

		BigDecimal basePrice = BigDecimal.valueOf(100);
		LocalDate in = LocalDate.of(2026, 6, 1);
		LocalDate out = LocalDate.of(2026, 6, 4); // 3 nights: 1st, 2nd(rule), 3rd

		// Act
		PriceBreakdownDTO dto = pricingCalculator.calculateBreakdown(basePrice, in, out);

		// Assert
		// totals: 100 + 150 + 100 = 350
		assertEquals(3, dto.getNights());
		assertEquals(new BigDecimal("350.00"), dto.getSubtotal());
		assertEquals(new BigDecimal("56.00"), dto.getTaxAmount());
		assertEquals(new BigDecimal("406.00"), dto.getTotalPrice());

		// التحقق من الليلة التي انطبق عليها الـ rule
		assertEquals("Promo", dto.getBreakdown().get(1).getAppliedRuleName());

		// نستخدم compareTo بدل assertEquals لأن BigDecimal قد يكون 1.5 أو 1.50
		assertEquals(
				0,
				new BigDecimal("1.50").compareTo(dto.getBreakdown().get(1).getSeasonMultiplier())
		);
	}

	@Test
	void calculateBreakdown_weekendMultiplierApplied_onWeekendNight() {
		// Arrange
		// لا يوجد seasonal rules
		when(pricingRuleRepository.findActiveRulesInRange(any(), any()))
				.thenReturn(Collections.emptyList());

		BigDecimal basePrice = BigDecimal.valueOf(100);
		LocalDate in = LocalDate.of(2026, 6, 4);   // Thursday
		LocalDate out = LocalDate.of(2026, 6, 6);  // Thu, Fri nights

		// Act
		PriceBreakdownDTO dto = pricingCalculator.calculateBreakdown(basePrice, in, out);

		// Assert
		// Thursday = 100
		// Friday = 125
		// subtotal = 225
		assertEquals(2, dto.getNights());
		assertEquals(new BigDecimal("225.00"), dto.getSubtotal());
		assertEquals(new BigDecimal("36.00"), dto.getTaxAmount());
		assertEquals(new BigDecimal("261.00"), dto.getTotalPrice());

		// الليلة الثانية هي Friday => weekend
		assertTrue(dto.getBreakdown().get(1).isWeekend());
		assertEquals(
				0,
				new BigDecimal("1.25").compareTo(dto.getBreakdown().get(1).getWeekendMultiplier())
		);
	}

	@Test
	void calculateTotalPrice_delegatesToBreakdown() {
		// Arrange
		// نعمل spy حتى نراقب استدعاء calculateBreakdown
		PricingCalculator spy = spy(pricingCalculator);

		PriceBreakdownDTO fake = new PriceBreakdownDTO(
				BigDecimal.valueOf(100),
				1,
				Collections.emptyList(),
				BigDecimal.valueOf(100),
				BigDecimal.valueOf(0.16),
				BigDecimal.valueOf(16),
				BigDecimal.valueOf(116)
		);

		doReturn(fake).when(spy).calculateBreakdown(any(), any(), any());

		// Act
		BigDecimal total = spy.calculateTotalPrice(
				BigDecimal.valueOf(100),
				LocalDate.now(),
				LocalDate.now().plusDays(1)
		);

		// Assert
		assertEquals(new BigDecimal("116"), total);
		verify(spy).calculateBreakdown(any(), any(), any());
	}

	@Test
	void calculateBreakdown_roundingIsTwoDecimals() {
		// Arrange
		when(pricingRuleRepository.findActiveRulesInRange(any(), any()))
				.thenReturn(Collections.emptyList());

		// نعيد ضبط taxRate للتأكد من التقريب
		setPrivateFieldUnchecked("taxRate", BigDecimal.valueOf(0.16));

		BigDecimal basePrice = new BigDecimal("100.333");
		LocalDate in = LocalDate.of(2026, 6, 1);
		LocalDate out = LocalDate.of(2026, 6, 2); // ليلة واحدة

		// Act
		PriceBreakdownDTO dto = pricingCalculator.calculateBreakdown(basePrice, in, out);

		// Assert
		// 100.333 -> 100.33
		// 100.33 * 0.16 = 16.0528 -> 16.05
		// total = 116.38
		assertEquals(new BigDecimal("100.33"), dto.getSubtotal());
		assertEquals(new BigDecimal("16.05"), dto.getTaxAmount());
		assertEquals(new BigDecimal("116.38"), dto.getTotalPrice());
	}

	/**
	 * helper ثاني لنفس فكرة setPrivateField
	 * لكن بدون throws checked exception
	 */
	private void setPrivateFieldUnchecked(String name, Object value) {
		try {
			Field f = PricingCalculator.class.getDeclaredField(name);
			f.setAccessible(true);
			f.set(pricingCalculator, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}