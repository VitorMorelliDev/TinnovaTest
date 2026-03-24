package com.tinnova.vehicleapi.service;

import com.tinnova.vehicleapi.domain.entity.Vehicle;
import com.tinnova.vehicleapi.exception.DuplicateLicensePlateException;
import com.tinnova.vehicleapi.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        BigDecimal exchangeRate = new BigDecimal("5.00");

        when(vehicleRepository.existsByLicensePlate("ABC-1234")).thenReturn(false);
        when(exchangeRateService.getUsdToBrlRate()).thenReturn(exchangeRate);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        Vehicle savedVehicle = vehicleService.create(vehicle, priceInBrl);

        assertNotNull(savedVehicle);
        assertEquals(new BigDecimal("10000.00"), savedVehicle.getPrice()); // 50000 / 5
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw exception when license plate already exists")
    void shouldThrowExceptionWhenPlateExists() {
        Vehicle vehicle = Vehicle.builder().licensePlate("DUP-1234").build();
        when(vehicleRepository.existsByLicensePlate("DUP-1234")).thenReturn(true);

        assertThrows(DuplicateLicensePlateException.class, () ->
                vehicleService.create(vehicle, BigDecimal.TEN)
        );
        verify(vehicleRepository, never()).save(any());
    }
}