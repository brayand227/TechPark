package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.RutaDTO;
import com.techpark.techpark_uq.model.dto.RutaMultipleDTO;
import com.techpark.techpark_uq.model.dto.SolicitudRutaDTO;
import com.techpark.techpark_uq.service.RutaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RutaController {
    
    private final RutaService rutaService;
    
    /**
     * Encontrar ruta más corta entre dos atracciones
     */
    @PostMapping("/mas-corta")
    public ResponseEntity<ApiResponseDTO<RutaDTO>> encontrarRutaMasCorta(
            @Valid @RequestBody SolicitudRutaDTO solicitud,
            HttpServletRequest request) {
        
        RutaDTO ruta = rutaService.encontrarRutaMasCorta(solicitud);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            ruta,
            "Ruta encontrada exitosamente",
            request.getRequestURI()
        ));
    }
    
    /**
     * Planificar ruta para múltiples atracciones (tour)
     */
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponseDTO<RutaMultipleDTO>> planificarRutaMultiple(
            @RequestBody List<Long> atraccionesIds,
            HttpServletRequest request) {
        
        RutaMultipleDTO rutaMultiple = rutaService.encontrarRutaMultiple(atraccionesIds);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            rutaMultiple,
            "Ruta múltiple planificada",
            request.getRequestURI()
        ));
    }
    
    /**
     * Encontrar atracciones cercanas a una referencia
     */
    @GetMapping("/cercanas/{atraccionId}/{limite}")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> encontrarCercanas(
            @PathVariable Long atraccionId,
            @PathVariable int limite,
            HttpServletRequest request) {
        
        List<Map<String, Object>> cercanas = rutaService.encontrarAtraccionesCercanas(atraccionId, limite);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            cercanas,
            "Atracciones cercanas encontradas",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener estado del mapa (conectividad, clusters)
     */
    @GetMapping("/estado-mapa")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerEstadoMapa(
            HttpServletRequest request) {
        
        Map<String, Object> estado = rutaService.obtenerEstadoMapa();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            estado,
            "Estado del mapa obtenido",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener mapa visual en formato JSON (para frontend)
     */
    @GetMapping("/mapa-visual")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerMapaVisual(
            HttpServletRequest request) {
        
        Map<String, Object> mapa = rutaService.obtenerMapaVisual();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            mapa,
            "Mapa visual generado",
            request.getRequestURI()
        ));
    }
    
    /**
     * Refrescar el mapa (después de agregar/editar atracciones)
     */
    @PostMapping("/refrescar")
    public ResponseEntity<ApiResponseDTO<String>> refrescarMapa(
            HttpServletRequest request) {
        
        rutaService.refrescarMapa();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            "Mapa refrescado exitosamente",
            "Mapa actualizado con las nuevas atracciones",
            request.getRequestURI()
        ));
    }
}