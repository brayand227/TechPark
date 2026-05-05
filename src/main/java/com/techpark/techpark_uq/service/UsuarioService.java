package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.model.entity.RolUsuario;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.model.entity.Visitante;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import com.techpark.techpark_uq.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioDTO registrarVisitante(RegistroVisitanteRequest request) {
        // Validar email único
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado", "EMAIL_EXISTENTE");
        }

        // Validar documento único
        if (usuarioRepository.existsByDocumento(request.getDocumento())) {
            throw new BusinessException("El documento ya está registrado", "DOCUMENTO_EXISTENTE");
        }

        // Crear visitante
        Visitante visitante = new Visitante();
        visitante.setNombre(request.getNombre());
        visitante.setDocumento(request.getDocumento());
        visitante.setEmail(request.getEmail());
        visitante.setPassword(passwordEncoder.encode(request.getPassword()));
        visitante.setEdad(request.getEdad());
        visitante.setEstatura(request.getEstatura());
        visitante.setRol(RolUsuario.VISITANTE);
        visitante.setActivo(true);
        visitante.setSaldoVirtual(0.0);
        visitante.setTicketActivo(request.getTipoTicket());
        visitante.setUbicacionActual("Entrada Principal");

        Visitante saved = usuarioRepository.save(visitante);
        return usuarioMapper.toDto(saved);
    }

    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        return usuarioMapper.toDto(usuario);
    }

    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        return usuarioMapper.toDto(usuario);
    }

    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario obtenerUsuarioEntityPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
    }
}