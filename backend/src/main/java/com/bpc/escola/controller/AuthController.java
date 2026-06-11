package com.bpc.escola.controller;

import com.bpc.escola.dto.AuthResponse;
import com.bpc.escola.dto.LoginRequest;
import com.bpc.escola.dto.UsuarioDTO;
import com.bpc.escola.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UsuarioDTO me(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long xUsuarioId) {
        return authService.me(authorization, xUsuarioId);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
    }
}
