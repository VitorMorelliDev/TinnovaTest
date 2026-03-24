package com.tinnova.vehicleapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@FeignClient(name = "primaryExchange", url = "${api.exchange.primary-url}")
public interface PrimaryExchangeClient {
    @GetMapping
    Map<String, Map<String, String>> getExchangeRate();
}