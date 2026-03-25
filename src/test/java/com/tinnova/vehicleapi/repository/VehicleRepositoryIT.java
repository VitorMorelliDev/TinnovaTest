package com.tinnova.vehicleapi.repository;

import com.tinnova.vehicleapi.BaseIntegrationTest;
import com.tinnova.vehicleapi.domain.entity.Vehicle;
import com.tinnova.vehicleapi.repository.specification.VehicleSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private VehicleRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

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

    @Test
    @DisplayName("Should group and count vehicles by brand")
    void shouldCountVehiclesByBrand() {
        repository.save(createValidVehicle("Chevrolet", "Prata", "CHV-1111"));
        repository.save(createValidVehicle("Chevrolet", "Preto", "CHV-2222"));
        repository.save(createValidVehicle("Volkswagen", "Branco", "VWX-3333"));

        List<Object[]> report = repository.countVehiclesByBrand();

        assertEquals(2, report.size());
        long chevroletCount = report.stream().filter(r -> r[0].equals("Chevrolet")).mapToLong(r -> (Long) r[1]).findFirst().orElse(0L);
        long vwCount = report.stream().filter(r -> r[0].equals("Volkswagen")).mapToLong(r -> (Long) r[1]).findFirst().orElse(0L);

        assertEquals(2L, chevroletCount);
        assertEquals(1L, vwCount);
    }

    @Test
    @DisplayName("Should check if license plate exists")
    void shouldCheckIfExistsByLicensePlate() {
        repository.save(createValidVehicle("Hyundai", "Cinza", "HYU-9999"));

        boolean exists = repository.existsByLicensePlate("HYU-9999");
        boolean doesNotExist = repository.existsByLicensePlate("GHS-0000");

        assertTrue(exists);
        assertFalse(doesNotExist);
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