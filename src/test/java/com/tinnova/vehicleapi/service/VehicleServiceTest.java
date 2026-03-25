package com.tinnova.vehicleapi.service;

import com.tinnova.vehicleapi.domain.entity.Vehicle;
import com.tinnova.vehicleapi.exception.DuplicateLicensePlateException;
import com.tinnova.vehicleapi.exception.ResourceNotFoundException;
import com.tinnova.vehicleapi.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    @DisplayName("Should create a vehicle successfully with BRL to USD conversion")
    void shouldCreateVehicleSuccessfully() {
        Vehicle vehicle = Vehicle.builder().licensePlate("ABC-1234").build();
        BigDecimal priceInBrl = new BigDecimal("50000.00");

        when(vehicleRepository.existsByLicensePlate("ABC-1234")).thenReturn(false);
        when(exchangeRateService.getUsdToBrlRate()).thenReturn(new BigDecimal("5.00"));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        Vehicle savedVehicle = vehicleService.create(vehicle, priceInBrl);

        assertNotNull(savedVehicle);
        assertEquals(new BigDecimal("10000.00"), savedVehicle.getPrice());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw exception when creating with an existing license plate")
    void shouldThrowExceptionWhenCreatingWithExistingPlate() {
        Vehicle vehicle = Vehicle.builder().licensePlate("DUP-1234").build();
        when(vehicleRepository.existsByLicensePlate("DUP-1234")).thenReturn(true);

        assertThrows(DuplicateLicensePlateException.class, () -> vehicleService.create(vehicle, BigDecimal.TEN));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should return a page of vehicles")
    @SuppressWarnings("unchecked")
    void shouldReturnPageOfVehicles() {
        Page<Vehicle> page = new PageImpl<>(Collections.singletonList(Vehicle.builder().build()));
        Specification<Vehicle> spec = mock(Specification.class);
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vehicle> result = vehicleService.findAll(spec, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(vehicleRepository).findAll(spec, pageRequest);
    }

    @Test
    @DisplayName("Should return active vehicle by ID")
    void shouldReturnActiveVehicleById() {
        Vehicle vehicle = Vehicle.builder().id(1L).active(true).build();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle found = vehicleService.findById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Should throw exception when vehicle is inactive (soft deleted)")
    void shouldThrowExceptionWhenVehicleIsInactive() {
        Vehicle vehicle = Vehicle.builder().id(1L).active(false).build();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.findById(1L));
    }

    @Test
    @DisplayName("Should throw exception when vehicle is not found")
    void shouldThrowExceptionWhenVehicleNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.findById(99L));
    }

    @Test
    @DisplayName("Should update vehicle and recalculate price when BRL price is provided")
    void shouldUpdateVehicleAndRecalculatePrice() {
        Vehicle existing = Vehicle.builder()
                .id(1L)
                .licensePlate("OLD-1111")
                .price(new BigDecimal("5000.00"))
                .active(true)
                .build();

        Vehicle updateData = Vehicle.builder()
                .brand("Toyota")
                .licensePlate("OLD-1111")
                .modelYear(2025)
                .color("Blue")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRateService.getUsdToBrlRate()).thenReturn(new BigDecimal("5.00"));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        Vehicle updated = vehicleService.update(1L, updateData, new BigDecimal("50000.00"));

        assertEquals("Toyota", updated.getBrand());
        assertEquals("Blue", updated.getColor());
        assertEquals(2025, updated.getModelYear());
        assertEquals(new BigDecimal("10000.00"), updated.getPrice());
    }

    @Test
    @DisplayName("Should update vehicle without changing price when BRL price is null (PATCH scenario)")
    void shouldUpdateVehicleWithoutChangingPrice() {
        BigDecimal originalUsdPrice = new BigDecimal("5000.00");
        Vehicle existing = Vehicle.builder()
                .id(1L)
                .licensePlate("OLD-1111")
                .price(originalUsdPrice)
                .active(true)
                .build();

        Vehicle updateData = Vehicle.builder()
                .brand("Honda")
                .licensePlate("OLD-1111")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        Vehicle updated = vehicleService.update(1L, updateData, null);

        assertEquals("Honda", updated.getBrand());
        assertEquals(originalUsdPrice, updated.getPrice());
        verify(exchangeRateService, never()).getUsdToBrlRate();
    }

    @Test
    @DisplayName("Should validate license plate when updating to a new plate")
    void shouldValidatePlateWhenUpdatingToNewPlate() {
        Vehicle existing = Vehicle.builder().id(1L).licensePlate("OLD-1111").active(true).build();
        Vehicle updateData = Vehicle.builder().licensePlate("NEW-2222").build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.existsByLicensePlate("NEW-2222")).thenReturn(true);

        assertThrows(DuplicateLicensePlateException.class, () -> vehicleService.update(1L, updateData, null));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should perform soft delete on vehicle")
    void shouldPerformSoftDelete() {
        Vehicle existing = Vehicle.builder().id(1L).active(true).build();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        vehicleService.delete(1L);

        assertFalse(existing.getActive());
        verify(vehicleRepository).save(existing);
    }

    @Test
    @DisplayName("Should return vehicle count by brand")
    void shouldReturnVehiclesCountByBrand() {
        List<Object[]> expectedResult = new ArrayList<>();
        expectedResult.add(new Object[]{"Toyota", 5L});
        expectedResult.add(new Object[]{"Ford", 3L});

        when(vehicleRepository.countVehiclesByBrand()).thenReturn(expectedResult);

        List<Object[]> result = vehicleService.getVehiclesCountByBrand();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Toyota", result.getFirst()[0]);
        assertEquals(5L, result.getFirst()[1]);
        verify(vehicleRepository).countVehiclesByBrand();
    }
}