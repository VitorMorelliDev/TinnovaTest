package com.tinnova.vehicleapi.service;

import com.tinnova.vehicleapi.client.FallbackExchangeClient;
import com.tinnova.vehicleapi.client.PrimaryExchangeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final PrimaryExchangeClient primaryClient;
    private final FallbackExchangeClient fallbackClient;

    @Cacheable(value = "dollar_rate", key = "'usd_to_brl'")
    public BigDecimal getUsdToBrlRate() {
        log.info("Fetching USD rate from external APIs (Cache miss)");
        try {
            return fetchFromPrimaryApi();
        } catch (Exception e) {
            log.warn("Primary API failed, attempting fallback API. Error: {}", e.getMessage());
            return fetchFromFallbackApi();
        }
    }

    private BigDecimal fetchFromPrimaryApi() {
        Map<String, Map<String, String>> response = primaryClient.getExchangeRate();
        if (response != null && response.containsKey("USDBRL")) {
            return new BigDecimal(response.get("USDBRL").get("bid"));
        }
        throw new RuntimeException("Invalid response from primary API");
    }

    private BigDecimal fetchFromFallbackApi() {
        Map<String, Object> response = fallbackClient.getFallbackRate();
        if (response != null && response.containsKey("rates")) {
            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            return BigDecimal.valueOf(rates.get("BRL"));
        }
        throw new RuntimeException("Invalid response from fallback API");
    }
}