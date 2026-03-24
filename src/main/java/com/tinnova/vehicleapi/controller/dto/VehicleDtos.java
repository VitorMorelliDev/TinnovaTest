package com.tinnova.vehicleapi.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tinnova.vehicleapi.domain.entity.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record VehicleDtos() {

    @Schema(description = "Vehicle details for response")
    public record VehicleResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("marca") String brand,
            @JsonProperty("ano") Integer modelYear,
            @JsonProperty("cor") String color,
            @JsonProperty("placa") String licensePlate,
            @JsonProperty("preco_usd") @Schema(description = "Price stored in USD") BigDecimal price,
            @JsonProperty("ativo") Boolean active
    ) {
        public static VehicleResponse fromEntity(Vehicle vehicle) {
            return new VehicleResponse(
                    vehicle.getId(),
                    vehicle.getBrand(),
                    vehicle.getModelYear(),
                    vehicle.getColor(),
                    vehicle.getLicensePlate(),
                    vehicle.getPrice(),
                    vehicle.getActive()
            );
        }
    }

    @Schema(description = "Data for vehicle registration and full update")
    public record VehicleRequest(
            @JsonProperty("marca")
            @NotBlank(message = "Brand is required")
            @Schema(example = "Toyota", description = "Vehicle brand")
            String brand,

            @JsonProperty("ano")
            @NotNull(message = "Model year is required")
            @Schema(example = "2024", description = "Vehicle model year")
            Integer modelYear,

            @JsonProperty("cor")
            @NotBlank(message = "Color is required")
            @Schema(example = "Preto", description = "Vehicle color")
            String color,

            @JsonProperty("placa")
            @NotBlank(message = "License plate is required")
            @Schema(example = "ABC-1234", description = "Unique license plate")
            String licensePlate,

            @JsonProperty("preco")
            @NotNull(message = "Price is required")
            @Positive(message = "Price must be positive")
            @Schema(example = "150000.00", description = "Price in Reais (BRL)")
            BigDecimal priceInBrl
    ) {}

    @Schema(description = "Data for partial vehicle update")
    public record VehiclePatchRequest(
            @JsonProperty("marca")
            @Schema(example = "Toyota", description = "New brand (optional)")
            String brand,

            @JsonProperty("ano")
            @Schema(example = "2024", description = "New model year (optional)")
            Integer modelYear,

            @JsonProperty("cor")
            @Schema(example = "Azul", description = "New color (optional)")
            String color,

            @JsonProperty("preco")
            @Positive(message = "Price must be positive if provided")
            @Schema(example = "155000.00", description = "New price in Reais (BRL) (optional)")
            BigDecimal priceInBrl
    ) {}
}