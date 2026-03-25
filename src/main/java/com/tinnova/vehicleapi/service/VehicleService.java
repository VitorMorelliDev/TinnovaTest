package com.tinnova.vehicleapi.service;

import com.tinnova.vehicleapi.domain.entity.Vehicle;
import com.tinnova.vehicleapi.exception.DuplicateLicensePlateException;
import com.tinnova.vehicleapi.exception.ResourceNotFoundException;
import com.tinnova.vehicleapi.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public Vehicle create(Vehicle vehicle, BigDecimal priceInBrl) {
        validateLicensePlateUniqueness(vehicle.getLicensePlate());

        BigDecimal usdRate = exchangeRateService.getUsdToBrlRate();
        BigDecimal priceInUsd = priceInBrl.divide(usdRate, 2, RoundingMode.HALF_UP);
        vehicle.setPrice(priceInUsd);

        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> findAll(Specification<Vehicle> spec, Pageable pageable) {
        return vehicleRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .filter(Vehicle::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    @Transactional
    public Vehicle update(Long id, Vehicle updatedData, BigDecimal priceInBrl) {
        Vehicle existingVehicle = findById(id);

        if (priceInBrl != null) {
            BigDecimal usdRate = exchangeRateService.getUsdToBrlRate();
            BigDecimal priceInUsd = priceInBrl.divide(usdRate, 2, RoundingMode.HALF_UP);
            existingVehicle.setPrice(priceInUsd);
        }

        if (updatedData != null) {
            if (!existingVehicle.getLicensePlate().equals(updatedData.getLicensePlate())) {
                validateLicensePlateUniqueness(updatedData.getLicensePlate());
            }
            existingVehicle.setBrand(updatedData.getBrand());
            existingVehicle.setModelYear(updatedData.getModelYear());
            existingVehicle.setColor(updatedData.getColor());
            existingVehicle.setLicensePlate(updatedData.getLicensePlate());
        }

        return vehicleRepository.save(existingVehicle);
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findById(id);
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    public List<Object[]> getVehiclesCountByBrand() {
        return vehicleRepository.countVehiclesByBrand();
    }

    private void validateLicensePlateUniqueness(String licensePlate) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new DuplicateLicensePlateException("License plate already exists: " + licensePlate);
        }
    }
}