package com.tinnova.vehicleapi.config;

import com.tinnova.vehicleapi.domain.entity.User;
import com.tinnova.vehicleapi.domain.enums.Role;
import com.tinnova.vehicleapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Banco de dados vazio. Criando usuários padrão para o teste...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(admin);
            userRepository.save(user);

            log.info("Usuários criados com sucesso!");
            log.info("=========================================================");
            log.info("ADMIN -> Username: admin | Password: admin123");
            log.info("USER  -> Username: user  | Password: user123");
            log.info("=========================================================");
        } else {
            log.info("Usuários já existem no banco de dados. Pulando a etapa de Seed.");
        }
    }
}