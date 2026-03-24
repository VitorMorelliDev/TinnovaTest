package com.tinnova.vehicleapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@FeignClient(name = "fallbackExchange", url = "${api.exchange.fallback-url}")
public interface FallbackExchangeClient {
    @GetMapping
    Map<String, Object> getFallbackRate();
}