package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.LoginRequestDTO;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.security.JwtTokenProvider;
import com.techpark.techpark_uq.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider tokenProvider;
        private final UsuarioService usuarioService;

        @PostMapping("/login")
        public ResponseEntity<ApiResponseDTO<Map<String, Object>>> login(
                        @Valid @RequestBody LoginRequestDTO loginRequest,
                        HttpServletRequest request) {

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginRequest.getEmail(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = tokenProvider.generarToken(authentication);

                UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(loginRequest.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("token", jwt);
                response.put("id", usuario.getId());
                response.put("nombre", usuario.getNombre());
                response.put("email", usuario.getEmail());
                response.put("rol", usuario.getRol());

                return ResponseEntity.ok(ApiResponseDTO.success(
                                response,
                                "Login exitoso",
                                request.getRequestURI()));
        }

        @PostMapping("/registro")
        public ResponseEntity<ApiResponseDTO<UsuarioDTO>> registrar(
                        @Valid @RequestBody RegistroVisitanteRequest registroRequest,
                        HttpServletRequest request) {

                UsuarioDTO nuevoUsuario = usuarioService.registrarVisitante(registroRequest);

                return ResponseEntity.ok(ApiResponseDTO.success(
                                nuevoUsuario,
                                "Usuario registrado exitosamente",
                                request.getRequestURI()));
        }

}