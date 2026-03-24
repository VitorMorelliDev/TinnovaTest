package com.tinnova.vehicleapi.repository;

import com.tinnova.vehicleapi.BaseIntegrationTest;
import com.tinnova.vehicleapi.domain.entity.Vehicle;
import com.tinnova.vehicleapi.repository.specification.VehicleSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private VehicleRepository repository;

    @Test
    @DisplayName("Should filter vehicles by brand and color simultaneously")
    void shouldFilterByCombinedCriteria() {
        repository.save(createValidVehicle("Ford", "Blue", "AAA-1111"));
        repository.save(createValidVehicle("Ford", "Black", "BBB-2222"));

        Specification<Vehicle> spec = VehicleSpecification.getVehiclesByFilters("Ford", null, "Blue", null, null);
        List<Vehicle> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Blue", result.getFirst().getColor());
    }

    @Test
    @DisplayName("Should enforce database unique constraint for license plate")
    void shouldEnforceUniqueLicensePlate() {
        repository.save(createValidVehicle("Toyota", "White", "UNI-1234"));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(createValidVehicle("Honda", "Black", "UNI-1234")));
    }

    private Vehicle createValidVehicle(String brand, String color, String plate) {
        return Vehicle.builder()
                .brand(brand)
                .color(color)
                .licensePlate(plate)
                .modelYear(2024)
                .price(BigDecimal.valueOf(50000))
                .active(true)
                .build();
    }
}