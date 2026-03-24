package com.tinnova.vehicleapi.controller;

import com.tinnova.vehicleapi.controller.dto.AuthDtos.LoginRequest;
import com.tinnova.vehicleapi.controller.dto.AuthDtos.TokenResponse;
import com.tinnova.vehicleapi.domain.entity.User;
import com.tinnova.vehicleapi.repository.UserRepository;
import com.tinnova.vehicleapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication"));

        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }
}