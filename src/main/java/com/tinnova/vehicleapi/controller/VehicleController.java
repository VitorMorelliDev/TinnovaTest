package com.tinnova.vehicleapi.controller;

import com.tinnova.vehicleapi.controller.dto.VehicleDtos.VehiclePatchRequest;
import com.tinnova.vehicleapi.controller.dto.VehicleDtos.VehicleRequest;
import com.tinnova.vehicleapi.controller.dto.VehicleDtos.VehicleResponse;
import com.tinnova.vehicleapi.domain.entity.Vehicle;
import com.tinnova.vehicleapi.repository.specification.VehicleSpecification;
import com.tinnova.vehicleapi.service.VehicleService;
import com.tinnova.vehicleapi.util.SortUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Endpoints for vehicle management")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new vehicle (ADMIN only)")
    public VehicleResponse create(@RequestBody @Valid VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .brand(request.brand())
                .modelYear(request.modelYear())
                .color(request.color())
                .licensePlate(request.licensePlate())
                .build();

        Vehicle savedVehicle = vehicleService.create(vehicle, request.priceInBrl());
        return VehicleResponse.fromEntity(savedVehicle);
    }

    @GetMapping
    @Operation(summary = "List vehicles with dynamic filters and pagination")
    @Parameters({
            @Parameter(name = "page", description = "Page index (0..N)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "Items per page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20")),
            @Parameter(
                    name = "sortBy",
                    description = "Field to sort by",
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "string", allowableValues = {"marca", "ano", "cor", "placa", "preco"}, defaultValue = "marca")
            ),
            @Parameter(
                    name = "direction",
                    description = "Sort direction",
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "asc")
            )
    })
    public Page<VehicleResponse> findAll(
            @RequestParam(name = "marca", required = false) String brand,
            @RequestParam(name = "ano", required = false) Integer modelYear,
            @RequestParam(name = "cor", required = false) String color,
            @RequestParam(name = "minPreco", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPreco", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sortBy", defaultValue = "marca") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {

        Pageable translatedPageable = SortUtils.buildTranslatedPageable(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction
        );

        Specification<Vehicle> spec = VehicleSpecification.getVehiclesByFilters(
                brand, modelYear, color, minPrice, maxPrice
        );

        return vehicleService.findAll(spec, translatedPageable).map(VehicleResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information about a specific vehicle")
    public VehicleResponse findById(@PathVariable Long id) {
        return VehicleResponse.fromEntity(vehicleService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of a vehicle (ADMIN only)")
    public VehicleResponse update(@PathVariable Long id, @RequestBody @Valid VehicleRequest request) {
        Vehicle vehicleData = Vehicle.builder()
                .brand(request.brand())
                .modelYear(request.modelYear())
                .color(request.color())
                .licensePlate(request.licensePlate())
                .build();

        Vehicle updatedVehicle = vehicleService.update(id, vehicleData, request.priceInBrl());
        return VehicleResponse.fromEntity(updatedVehicle);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update of a vehicle (ADMIN only)")
    public VehicleResponse patch(@PathVariable Long id, @RequestBody @Valid VehiclePatchRequest request) {
        Vehicle existing = vehicleService.findById(id);

        if (request.brand() != null) existing.setBrand(request.brand());
        if (request.modelYear() != null) existing.setModelYear(request.modelYear());
        if (request.color() != null) existing.setColor(request.color());

        Vehicle patchedVehicle = vehicleService.update(id, existing, request.priceInBrl());

        return VehicleResponse.fromEntity(patchedVehicle);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete a vehicle (ADMIN only)")
    public void delete(@PathVariable Long id) {
        vehicleService.delete(id);
    }

    @GetMapping("/relatorios/por-marca")
    @Operation(summary = "Get a report of vehicle counts grouped by brand")
    public List<Map<String, Object>> getReportByBrand() {
        List<Object[]> results = vehicleService.getVehiclesCountByBrand();
        return results.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("brand", obj[0]);
            map.put("count", obj[1]);
            return map;
        }).collect(Collectors.toList());
    }
}