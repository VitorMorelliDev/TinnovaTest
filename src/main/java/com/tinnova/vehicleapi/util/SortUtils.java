package com.tinnova.vehicleapi.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;

public class SortUtils {

    private static final Map<String, String> VEHICLE_SORT_MAP = Map.of(
            "marca", "brand",
            "ano", "modelYear",
            "cor", "color",
            "placa", "licensePlate",
            "preco", "price"
    );

    public static Pageable buildTranslatedPageable(int page, int size, String apiField, String direction) {
        String entityField = VEHICLE_SORT_MAP.getOrDefault(apiField.toLowerCase(), "brand");

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(sortDirection, entityField));
    }
}