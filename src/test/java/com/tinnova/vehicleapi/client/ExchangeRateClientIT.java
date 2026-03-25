package com.tinnova.vehicleapi.client;

import com.tinnova.vehicleapi.BaseIntegrationTest;
import com.tinnova.vehicleapi.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import java.math.BigDecimal;
import java.util.Objects;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(properties = {
        "api.exchange.primary-url=http://localhost:${wiremock.server.port}",
        "api.exchange.fallback-url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
class ExchangeRateClientIT extends BaseIntegrationTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset();
        Objects.requireNonNull(cacheManager.getCache("dollar_rate")).clear();
    }

    @Test
    @DisplayName("Should fetch from primary API successfully")
    void shouldFetchFromPrimaryApi() {
        stubFor(get(urlPathEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"USDBRL\": {\"bid\": \"5.25\"}}")));

        BigDecimal rate = exchangeRateService.getUsdToBrlRate();
        assertEquals(0, rate.compareTo(new BigDecimal("5.25")));
    }

    @Test
    @DisplayName("Should fallback to secondary API when primary fails")
    void shouldFallbackToSecondaryApi() {
        stubFor(get(urlPathEqualTo("/"))
                .inScenario("Fallback Scenario")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Failed Once"));

        stubFor(get(urlPathEqualTo("/"))
                .inScenario("Fallback Scenario")
                .whenScenarioStateIs("Failed Once")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"rates\": {\"BRL\": 5.30}}")));

        BigDecimal rate = exchangeRateService.getUsdToBrlRate();
        assertEquals(0, rate.compareTo(new BigDecimal("5.30")));
    }
}