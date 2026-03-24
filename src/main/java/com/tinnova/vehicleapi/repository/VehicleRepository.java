package com.tinnova.vehicleapi.repository;

import com.tinnova.vehicleapi.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByLicensePlate(String licensePlate);

    @Query("SELECT v.brand, COUNT(v) FROM Vehicle v WHERE v.active = true GROUP BY v.brand")
    List<Object[]> countVehiclesByBrand();
}