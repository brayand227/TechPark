package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.AlertaClimaDTO;
import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.SimulacionClimaDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.service.ClimaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clima")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClimaController {
    
    private final ClimaService climaService;
    
    /**
     * Activar alerta climática (Solo administrador)
     */
    @PostMapping("/activar/{administradorId}")
    public ResponseEntity<ApiResponseDTO<AlertaClimaDTO>> activarAlerta(
            @Valid @RequestBody SimulacionClimaDTO simulacion,
            @PathVariable Long administradorId,
            HttpServletRequest request) {
        
        AlertaClimaDTO alerta = climaService.activarAlertaClimatica(simulacion, administradorId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alerta,
            "Alerta climática activada. Atracciones afectadas cerradas.",
            request.getRequestURI()
        ));        
    }
    
    /**
     * Desactivar alerta climática (Solo administrador)
     */

    @PutMapping("/desactivar/{alertaId}/{administradorId}")
    public ResponseEntity<ApiResponseDTO<AlertaClimaDTO>> desactivarAlerta(
            @PathVariable Long alertaId,
            @PathVariable Long administradorId,
            HttpServletRequest request) {

        
        AlertaClimaDTO alerta = climaService.desactivarAlertaClimatica(alertaId, administradorId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alerta,
            "Alerta climática desactivada. Atracciones reactivadas.",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener alertas climáticas activas
     */
    @GetMapping("/activas")
    public ResponseEntity<ApiResponseDTO<List<AlertaClimaDTO>>> obtenerAlertasActivas(
            HttpServletRequest request) {
        
        List<AlertaClimaDTO> alertas = climaService.obtenerAlertasActivas();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alertas,
            "Alertas climáticas activas obtenidas",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener historial de alertas climáticas
     */
    @GetMapping("/historial")
    public ResponseEntity<ApiResponseDTO<List<AlertaClimaDTO>>> obtenerHistorial(
            HttpServletRequest request) {
        
        List<AlertaClimaDTO> historial = climaService.obtenerHistorialAlertas();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            historial,
            "Historial de alertas obtenido",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener atracciones cerradas por clima
     */
    @GetMapping("/atracciones-cerradas")
    public ResponseEntity<ApiResponseDTO<List<Atraccion>>> obtenerAtraccionesCerradasPorClima(
            HttpServletRequest request) {
        
        List<Atraccion> atracciones = climaService.obtenerAtraccionesCerradasPorClima();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones cerradas por clima obtenidas",
            request.getRequestURI()
        ));
    }
    
    /**
     * Verificar si hay alerta activa
     */
    @GetMapping("/hay-alerta")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> hayAlertaActiva(
            HttpServletRequest request) {
        
        boolean hayAlerta = climaService.hayAlertaActiva();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            Map.of("hayAlerta", hayAlerta),
            "Estado de alerta obtenido",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener estadísticas de clima
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerEstadisticas(
            HttpServletRequest request) {
        
        Map<String, Object> estadisticas = climaService.obtenerEstadisticasClima();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            estadisticas,
            "Estadísticas climáticas obtenidas",
            request.getRequestURI()
        ));
    }
}