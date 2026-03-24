package com.tinnova.vehicleapi.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AuthDtos() {

    @Schema(description = "Credentials required for login")
    public record LoginRequest(
            @JsonProperty("usuario")
            @NotBlank(message = "Username is required")
            @Schema(example = "admin", description = "The username for the account")
            String username,

            @JsonProperty("senha")
            @NotBlank(message = "Password is required")
            @Schema(example = "admin123", description = "The account password")
            String password
    ) {}

    @Schema(description = "Response containing the JWT access token")
    public record TokenResponse(
            @JsonProperty("token")
            @Schema(description = "The generated JWT token for Bearer authentication")
            String token
    ) {}
}