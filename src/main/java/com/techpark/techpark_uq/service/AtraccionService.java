package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.AtraccionDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.model.entity.EstadoAtraccion;
import com.techpark.techpark_uq.model.entity.Zona;
import com.techpark.techpark_uq.repository.AtraccionRepository;
import com.techpark.techpark_uq.repository.ZonaRepository;
import com.techpark.techpark_uq.mapper.AtraccionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtraccionService {
    
    private final AtraccionRepository atraccionRepository;
    private final ZonaRepository zonaRepository;
    private final AtraccionMapper atraccionMapper;
    
    // Obtener todas las atracciones
    public List<AtraccionDTO> listarTodas() {
        return atraccionRepository.findAll().stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    // Obtener atracción por ID
    public AtraccionDTO obtenerPorId(Long id) {
        Atraccion atraccion = atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        return atraccionMapper.toDto(atraccion);
    }
    
    // Crear nueva atracción
    @Transactional
    public AtraccionDTO crear(AtraccionDTO atraccionDTO) {
        
        // Validar que la zona existe
        Zona zona = zonaRepository.findById(atraccionDTO.getZonaId())
                .orElseThrow(() -> new BusinessException("Zona no encontrada", "ZONA_NO_ENCONTRADA"));
        
        Atraccion atraccion = atraccionMapper.toEntity(atraccionDTO);
        atraccion.setZona(zona);
        atraccion.setEstado(EstadoAtraccion.ACTIVA);
        atraccion.setContadorVisitantes(0);
        
        Atraccion saved = atraccionRepository.save(atraccion);
        return atraccionMapper.toDto(saved);
    }
    
    // Actualizar estado de atracción
    @Transactional
    public AtraccionDTO actualizarEstado(Long id, String nuevoEstado, String motivo) {
        Atraccion atraccion = atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        atraccion.setEstado(EstadoAtraccion.valueOf(nuevoEstado));
        if (motivo != null) {
            atraccion.setMotivoCierre(motivo);
        }
        
        return atraccionMapper.toDto(atraccionRepository.save(atraccion));
    }
    
    // Obtener atracciones por zona
    public List<AtraccionDTO> listarPorZona(Long zonaId) {
        return atraccionRepository.findByZonaId(zonaId).stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    // Obtener atracciones que necesitan mantenimiento (contador >= 500)
    public List<AtraccionDTO> obtenerNecesitanMantenimiento() {
        return atraccionRepository.findAtraccionesQueNecesitanMantenimiento().stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    // Incrementar contador de visitantes (importante para mantenimiento)
    @Transactional
    public void incrementarContador(Long atraccionId) {
        atraccionRepository.incrementarContadorVisitantes(atraccionId);
        
        // Verificar si necesita mantenimiento
        Atraccion atraccion = atraccionRepository.findById(atraccionId).get();
        if (atraccion.getContadorVisitantes() >= 500 && 
            atraccion.getEstado() != EstadoAtraccion.MANTENIMIENTO) {
            // Cambiar a mantenimiento automáticamente
            atraccion.setEstado(EstadoAtraccion.MANTENIMIENTO);
            atraccion.setMotivoCierre("Mantenimiento preventivo - 500 visitantes alcanzados");
            atraccionRepository.save(atraccion);
        }
    }
}