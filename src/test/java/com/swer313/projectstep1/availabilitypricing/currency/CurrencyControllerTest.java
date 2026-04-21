package com.swer313.projectstep1.availabilitypricing.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private RoomTypeRepository roomTypeRepository;

    @MockBean
    private PricingCalculator pricingCalculator;

    @MockBean
    private ExchangeRateRepository exchangeRateRepository;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getSupportedCurrencies_returnsList() throws Exception {
        ExchangeRate r = new ExchangeRate();
        r.setFromCurrency("USD");
        r.setToCurrency("ILS");
        r.setRate(new BigDecimal("3.67"));

        when(currencyService.getSupportedCurrencies()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromCurrency").value("USD"))
                .andExpect(jsonPath("$[0].toCurrency").value("ILS"))
                .andExpect(jsonPath("$[0].rate").value(3.67));
    }

    @Test
    void getRoomPriceInCurrency_success_convertsAndReturnsBothAmounts() throws Exception {
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setName("Deluxe");
        rt.setBasePrice(new BigDecimal("100.00"));
        rt.setStatus(RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(rt));

        PriceBreakdownDTO breakdown = new PriceBreakdownDTO(
                new BigDecimal("100.00"),
                2,
                List.of(),
                new BigDecimal("200.00"),
                new BigDecimal("0.16"),
                new BigDecimal("32.00"),
                new BigDecimal("232.00")
        );

        when(pricingCalculator.calculateBreakdown(
                eq(new BigDecimal("100.00")),
                eq(LocalDate.of(2026, 5, 2)),
                eq(LocalDate.of(2026, 5, 4))
        )).thenReturn(breakdown);

        when(currencyService.convert(
                eq(new BigDecimal("232.00")),
                eq("USD"),
                eq("ILS")
        )).thenReturn(new BigDecimal("851.44"));

        ExchangeRate er = new ExchangeRate();
        er.setRate(new BigDecimal("3.67"));

        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(eq("USD"), eq("ILS")))
                .thenReturn(Optional.of(er));

        mockMvc.perform(get("/api/currencies/room-types/1/price")
                        .param("checkIn", "2026-05-02")
                        .param("checkOut", "2026-05-04")
                        .param("currency", "ILS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomTypeId").value(1))
                .andExpect(jsonPath("$.roomTypeName").value("Deluxe"))
                .andExpect(jsonPath("$.nights").value(2))
                .andExpect(jsonPath("$.originalTotalUSD").value(232.00))
                .andExpect(jsonPath("$.convertedTotal").value(851.44))
                .andExpect(jsonPath("$.currency").value("ILS"))
                .andExpect(jsonPath("$.exchangeRate").value(3.67));
    }

    @Test
    void getRoomPriceInCurrency_invalidDates_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/currencies/room-types/1/price")
                        .param("checkIn", "2026-05-03")
                        .param("checkOut", "2026-05-03")
                        .param("currency", "USD"))
                .andExpect(status().isBadRequest());
    }
}