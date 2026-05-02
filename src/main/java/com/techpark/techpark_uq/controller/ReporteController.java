package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.*;
import com.techpark.techpark_uq.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {
    
    private final ReporteService reporteService;
    
    /**
     * Reporte de ingresos diarios
     */
    @GetMapping("/ingresos/diario/{fecha}")
    public ResponseEntity<ApiResponseDTO<ReporteIngresosDTO>> reporteIngresosDiario(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        
        ReporteIngresosDTO reporte = reporteService.generarReporteIngresos(fecha);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            reporte,
            "Reporte de ingresos generado",
            request.getRequestURI()
        ));
    }
    
    /**
     * Reporte de atracciones (más y menos visitadas)
     */
    @GetMapping("/atracciones")
    public ResponseEntity<ApiResponseDTO<ReporteAtraccionesDTO>> reporteAtracciones(
            HttpServletRequest request) {
        
        ReporteAtraccionesDTO reporte = reporteService.generarReporteAtracciones();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            reporte,
            "Reporte de atracciones generado",
            request.getRequestURI()
        ));
    }
    
    /**
     * Reporte de tiempos de espera
     */
    @GetMapping("/tiempos/{fecha}")
    public ResponseEntity<ApiResponseDTO<ReporteTiemposDTO>> reporteTiempos(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        
        ReporteTiemposDTO reporte = reporteService.generarReporteTiempos(fecha);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            reporte,
            "Reporte de tiempos generado",
            request.getRequestURI()
        ));
    }
    
    /**
     * Reporte de mantenimiento
     */
    @GetMapping("/mantenimiento")
    public ResponseEntity<ApiResponseDTO<ReporteMantenimientoDTO>> reporteMantenimiento(
            HttpServletRequest request) {
        
        ReporteMantenimientoDTO reporte = reporteService.generarReporteMantenimiento();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            reporte,
            "Reporte de mantenimiento generado",
            request.getRequestURI()
        ));
    }
    
    /**
     * Reporte de afluencia
     */
    @GetMapping("/afluencia/{fecha}")
    public ResponseEntity<ApiResponseDTO<ReporteAfluenciaDTO>> reporteAfluencia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        
        ReporteAfluenciaDTO reporte = reporteService.generarReporteAfluencia(fecha);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            reporte,
            "Reporte de afluencia generado",
            request.getRequestURI()
        ));
    }
    
    /**
     * Reporte general completo
     */
    @GetMapping("/general/{periodo}")
    public ResponseEntity<ApiResponseDTO<ReporteGeneralDTO>> reporteGeneral(
            @PathVariable String periodo,  // DIARIO, SEMANAL, MENSUAL
            HttpServletRequest request) {
        
        ReporteGeneralDTO reporte = reporteService.generarReporteGeneral(periodo);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            reporte,
            "Reporte general generado",
            request.getRequestURI()
        ));
    }
}