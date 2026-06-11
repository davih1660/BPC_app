package com.bpc.escola.service;

import com.bpc.escola.domain.AuthToken;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.dto.AuthResponse;
import com.bpc.escola.dto.LoginRequest;
import com.bpc.escola.dto.UsuarioDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AuthTokenRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AuthTokenRepository authTokenRepository;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos.", "AUTH_INVALID", HttpStatus.UNAUTHORIZED));

        String senha = usuario.getSenha() != null ? usuario.getSenha() : "123456";
        if (!senha.equals(request.senha())) {
            throw new BusinessException("E-mail ou senha inválidos.", "AUTH_INVALID", HttpStatus.UNAUTHORIZED);
        }

        String token = UUID.randomUUID().toString();
        authTokenRepository.save(AuthToken.builder()
                .token(token)
                .usuario(usuario)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build());

        return new AuthResponse(token, UsuarioDTO.from(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO me(String authorization, Long xUsuarioId) {
        Usuario usuario = resolverUsuario(authorization, xUsuarioId);
        return UsuarioDTO.from(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario resolverUsuario(String authorization, Long xUsuarioId) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            return authTokenRepository.findByToken(token)
                    .map(AuthToken::getUsuario)
                    .orElseThrow(() -> new BusinessException("Sessão inválida.", "AUTH_INVALID", HttpStatus.UNAUTHORIZED));
        }
        if (xUsuarioId != null) {
            return usuarioRepository.findById(xUsuarioId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO", HttpStatus.UNAUTHORIZED));
        }
        throw new BusinessException("Não autenticado.", "AUTH_REQUIRED", HttpStatus.UNAUTHORIZED);
    }

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authTokenRepository.deleteById(authorization.substring(7).trim());
        }
    }
}
