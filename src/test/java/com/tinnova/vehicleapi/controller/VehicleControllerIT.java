package com.tinnova.vehicleapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinnova.vehicleapi.controller.dto.AuthDtos.LoginRequest;
import com.tinnova.vehicleapi.controller.dto.VehicleDtos.VehiclePatchRequest;
import com.tinnova.vehicleapi.controller.dto.VehicleDtos.VehicleRequest;
import com.tinnova.vehicleapi.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        vehicleRepository.deleteAll();
        adminToken = obtainToken("admin", "admin123");
        userToken = obtainToken("user", "user123");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void fullE2EFlow() throws Exception {
        VehicleRequest request = new VehicleRequest("Toyota", 2024, "Silver", "TIN-2026", new BigDecimal("180000"));

        mockMvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.placa").value("TIN-2026"))
                .andExpect(jsonPath("$.preco_usd").value(34285.71));

        mockMvc.perform(get("/veiculos")
                        .header("Authorization", "Bearer " + userToken)
                        .param("marca", "Toyota")
                        .param("ordenarPor", "preco")
                        .param("direcao", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].marca").value("Toyota"))
                .andExpect(jsonPath("$.content[0].placa").value("TIN-2026"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void securityScenarios() throws Exception {
        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isUnauthorized());

        VehicleRequest request = new VehicleRequest("Ford", 2020, "Blue", "ABC-1234", new BigDecimal("50000"));
        mockMvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchValidation() throws Exception {
        VehicleRequest createReq = new VehicleRequest("Honda", 2022, "Red", "HND-1111", new BigDecimal("100000"));
        MvcResult createResult = mockMvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn();

        long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        VehiclePatchRequest patchReq = new VehiclePatchRequest(null, null, "Black", null);

        mockMvc.perform(patch("/veiculos/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Black"))
                .andExpect(jsonPath("$.marca").value("Honda"));
    }
}