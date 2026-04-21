package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.availabilitypricing.availability.PagedResponse;
import com.swer313.projectstep1.errors.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // يفعّل Mockito داخل JUnit 5
class PricingRuleServiceImplTest {

    @Mock
    private PricingRuleRepository pricingRuleRepository; // وهمي بدل الريبو الحقيقي

    @Mock
    private PricingRuleMapper pricingRuleMapper; // وهمي بدل المابر الحقيقي

    @Mock
    private PricingCalculator pricingCalculator; // وهمي بدل الكالكوليتر الحقيقي

    @InjectMocks
    private PricingRuleServiceImpl pricingRuleService;
    // الكلاس الذي نختبره فعليًا
    // Mockito يحقن الـ mocks أعلاه بداخله تلقائيًا

    @Test
    void createRule_validRequest_success() {
        // Arrange
        // نجهز request صحيح
        PricingRuleRequest request = new PricingRuleRequest();
        request.setName("Summer");
        request.setDescription("Summer pricing");
        request.setStartDate(LocalDate.of(2026, 6, 1));
        request.setEndDate(LocalDate.of(2026, 6, 10));
        request.setPriceMultiplier(BigDecimal.valueOf(1.50));

        // الكيان الذي سيُحفظ
        PricingRule entity = new PricingRule();
        entity.setName("Summer");
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());

        PricingRuleResponse response = mock(PricingRuleResponse.class);

        // لا يوجد overlap
        when(pricingRuleRepository.findOverlapping(
                request.getStartDate(),
                request.getEndDate(),
                null
        )).thenReturn(Collections.emptyList());

        // المابر يحول request -> entity
        when(pricingRuleMapper.toEntity(request)).thenReturn(entity);

        // الريبو يرجع نفس الكيان بعد الحفظ
        when(pricingRuleRepository.save(entity)).thenReturn(entity);

        // المابر يحول entity -> response
        when(pricingRuleMapper.toResponse(entity)).thenReturn(response);

        // Act
        PricingRuleResponse result = pricingRuleService.createRule(request);

        // Assert
        assertNotNull(result); // لازم يرجع response
        verify(pricingRuleRepository).findOverlapping(request.getStartDate(), request.getEndDate(), null);
        verify(pricingRuleRepository).save(entity);
        verify(pricingRuleMapper).toEntity(request);
        verify(pricingRuleMapper).toResponse(entity);
    }

    @Test
    void createRule_endDateNotAfterStartDate_throwsBadRequestException() {
        // Arrange
        PricingRuleRequest request = new PricingRuleRequest();
        request.setStartDate(LocalDate.of(2026, 6, 10));
        request.setEndDate(LocalDate.of(2026, 6, 10)); // endDate ليس بعد startDate

        // Act + Assert
        assertThrows(BadRequestException.class, () -> pricingRuleService.createRule(request));

        // نتأكد أنه ما وصل للحفظ
        verify(pricingRuleRepository, never()).save(any());
        verify(pricingRuleRepository, never()).findOverlapping(any(), any(), any());
    }

    @Test
    void createRule_overlappingRulesExist_throwsOverlappingPricingRuleException() {
        // Arrange
        PricingRuleRequest request = new PricingRuleRequest();
        request.setStartDate(LocalDate.of(2026, 6, 1));
        request.setEndDate(LocalDate.of(2026, 6, 10));

        PricingRule overlappingRule = new PricingRule();
        overlappingRule.setName("Winter Promo");
        overlappingRule.setStartDate(LocalDate.of(2026, 6, 5));
        overlappingRule.setEndDate(LocalDate.of(2026, 6, 15));

        when(pricingRuleRepository.findOverlapping(
                request.getStartDate(),
                request.getEndDate(),
                null
        )).thenReturn(List.of(overlappingRule));

        // Act + Assert
        assertThrows(
                OverlappingPricingRuleException.class,
                () -> pricingRuleService.createRule(request)
        );

        // بما أن في overlap، ممنوع يصير save
        verify(pricingRuleRepository, never()).save(any());
    }

    @Test
    void updateRule_partialUpdate_success() {
        // Arrange
        PricingRuleUpdateRequest update = new PricingRuleUpdateRequest();
        update.setName("New Name"); // فقط تغيير الاسم

        PricingRule existingRule = new PricingRule();
        existingRule.setName("Old Name");
        existingRule.setStartDate(LocalDate.of(2026, 6, 1));
        existingRule.setEndDate(LocalDate.of(2026, 6, 10));

        PricingRuleResponse response = mock(PricingRuleResponse.class);

        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.of(existingRule));
        when(pricingRuleRepository.save(existingRule)).thenReturn(existingRule);
        when(pricingRuleMapper.toResponse(existingRule)).thenReturn(response);

        // Act
        PricingRuleResponse result = pricingRuleService.updateRule(1L, update);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", existingRule.getName()); // نتأكد أن الاسم تغيّر
        verify(pricingRuleRepository).findById(1L);
        verify(pricingRuleRepository).save(existingRule);
        verify(pricingRuleMapper).toResponse(existingRule);
    }

    @Test
    void updateRule_ruleNotFound_throwsPricingRuleNotFoundException() {
        // Arrange
        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                PricingRuleNotFoundException.class,
                () -> pricingRuleService.updateRule(1L, new PricingRuleUpdateRequest())
        );

        verify(pricingRuleRepository).findById(1L);
        verify(pricingRuleRepository, never()).save(any());
    }

    @Test
    void updateRule_datesChangedAndOverlap_throwsOverlappingPricingRuleException() {
        // Arrange
        PricingRuleUpdateRequest update = new PricingRuleUpdateRequest();
        update.setStartDate(LocalDate.of(2026, 6, 1));
        update.setEndDate(LocalDate.of(2026, 6, 20));

        PricingRule existingRule = new PricingRule();
        existingRule.setStartDate(LocalDate.of(2026, 6, 1));
        existingRule.setEndDate(LocalDate.of(2026, 6, 10));

        PricingRule overlapping = new PricingRule();
        overlapping.setName("Other Rule");
        overlapping.setStartDate(LocalDate.of(2026, 6, 15));
        overlapping.setEndDate(LocalDate.of(2026, 6, 25));

        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.of(existingRule));
        when(pricingRuleRepository.findOverlapping(
                update.getStartDate(),
                update.getEndDate(),
                1L
        )).thenReturn(List.of(overlapping));

        // Act + Assert
        assertThrows(
                OverlappingPricingRuleException.class,
                () -> pricingRuleService.updateRule(1L, update)
        );

        verify(pricingRuleRepository, never()).save(any());
    }

    @Test
    void updateRule_datesChangedToInvalidRange_throwsBadRequestException() {
        // Arrange
        PricingRuleUpdateRequest update = new PricingRuleUpdateRequest();
        // endDate before startDate -> invalid
        update.setStartDate(LocalDate.of(2026, 6, 10));
        update.setEndDate(LocalDate.of(2026, 6, 5));

        PricingRule existingRule = new PricingRule();
        existingRule.setStartDate(LocalDate.of(2026, 6, 1));
        existingRule.setEndDate(LocalDate.of(2026, 6, 9));

        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.of(existingRule));

        // Act + Assert
        assertThrows(
                BadRequestException.class,
                () -> pricingRuleService.updateRule(1L, update)
        );

        verify(pricingRuleRepository, never()).save(any());
    }

    @Test
    void deleteRule_existingRule_success() {
        // Arrange
        PricingRule existingRule = new PricingRule();
        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.of(existingRule));

        // Act
        pricingRuleService.deleteRule(1L);

        // Assert
        verify(pricingRuleRepository).findById(1L);
        verify(pricingRuleRepository).delete(existingRule);
    }

    @Test
    void getById_existingId_success() {
        // Arrange
        PricingRule existingRule = new PricingRule();
        PricingRuleResponse response = mock(PricingRuleResponse.class);

        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.of(existingRule));
        when(pricingRuleMapper.toResponse(existingRule)).thenReturn(response);

        // Act
        PricingRuleResponse result = pricingRuleService.getById(1L);

        // Assert
        assertNotNull(result);
        verify(pricingRuleRepository).findById(1L);
        verify(pricingRuleMapper).toResponse(existingRule);
    }

    @Test
    void getById_nonExistingId_throwsPricingRuleNotFoundException() {
        // Arrange
        when(pricingRuleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                PricingRuleNotFoundException.class,
                () -> pricingRuleService.getById(1L)
        );
    }

    @Test
    void getAllRules_returnsPagedMappedResponse() {
        // Arrange
        PricingRule rule = new PricingRule();
        Page<PricingRule> page = new PageImpl<>(List.of(rule));

        when(pricingRuleRepository.findAllByOrderByStartDateDesc(any(Pageable.class)))
                .thenReturn(page);
        when(pricingRuleMapper.toResponse(rule)).thenReturn(mock(PricingRuleResponse.class));

        // Act
        PagedResponse<PricingRuleResponse> result = pricingRuleService.getAllRules(Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        verify(pricingRuleRepository).findAllByOrderByStartDateDesc(any(Pageable.class));
        verify(pricingRuleMapper).toResponse(rule);
    }

    @Test
    void getActiveRules_returnsPagedMappedResponse() {
        // Arrange
        PricingRule rule = new PricingRule();
        Page<PricingRule> page = new PageImpl<>(List.of(rule));

        when(pricingRuleRepository.findByActiveTrueOrderByStartDateDesc(any(Pageable.class)))
                .thenReturn(page);
        when(pricingRuleMapper.toResponse(rule)).thenReturn(mock(PricingRuleResponse.class));

        // Act
        PagedResponse<PricingRuleResponse> result = pricingRuleService.getActiveRules(Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        verify(pricingRuleRepository).findByActiveTrueOrderByStartDateDesc(any(Pageable.class));
        verify(pricingRuleMapper).toResponse(rule);
    }

    @Test
    void previewPrice_validRequest_success() {
        // Arrange
        BigDecimal basePrice = BigDecimal.valueOf(100);
        LocalDate checkIn = LocalDate.of(2026, 6, 1);
        LocalDate checkOut = LocalDate.of(2026, 6, 3);

        PriceBreakdownDTO breakdown = mock(PriceBreakdownDTO.class);

        when(pricingCalculator.calculateBreakdown(basePrice, checkIn, checkOut))
                .thenReturn(breakdown);

        // Act
        PriceBreakdownDTO result = pricingRuleService.previewPrice(basePrice, checkIn, checkOut);

        // Assert
        assertNotNull(result);
        verify(pricingCalculator).calculateBreakdown(basePrice, checkIn, checkOut);
    }

    @Test
    void previewPrice_basePriceNull_throwsBadRequestException() {
        // basePrice null لازم يرمي exception
        assertThrows(
                BadRequestException.class,
                () -> pricingRuleService.previewPrice(null,
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 3))
        );
    }

    @Test
    void previewPrice_basePriceZeroOrLess_throwsBadRequestException() {
        // basePrice = 0 غير مسموح
        assertThrows(
                BadRequestException.class,
                () -> pricingRuleService.previewPrice(BigDecimal.ZERO,
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 3))
        );
    }

    @Test
    void previewPrice_checkOutNotAfterCheckIn_throwsBadRequestException() {
        // checkOut لازم يكون بعد checkIn
        assertThrows(
                BadRequestException.class,
                () -> pricingRuleService.previewPrice(
                        BigDecimal.valueOf(100),
                        LocalDate.of(2026, 6, 3),
                        LocalDate.of(2026, 6, 3)
                )
        );
    }
}