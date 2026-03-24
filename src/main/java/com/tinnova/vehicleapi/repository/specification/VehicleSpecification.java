package com.tinnova.vehicleapi.repository.specification;

import com.tinnova.vehicleapi.domain.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class VehicleSpecification {

    public static Specification<Vehicle> getVehiclesByFilters(
            String brand, Integer modelYear, String color, BigDecimal minPrice, BigDecimal maxPrice) {

        Specification<Vehicle> spec = (root, query, cb) -> cb.equal(root.get("active"), true);

        if (brand != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("brand"), brand));
        }
        if (modelYear != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("modelYear"), modelYear));
        }
        if (color != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("color"), color));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        return spec;
    }
}