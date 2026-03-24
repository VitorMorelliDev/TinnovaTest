package com.tinnova.vehicleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableFeignClients
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.tinnova.vehicleapi.repository")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class VehicleapiApplication {
	public static void main(String[] args) {
		SpringApplication.run(VehicleapiApplication.class, args);
	}
}
