package com.techpark.techpark_uq.controller;


import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.RespuestaColaDTO;
import com.techpark.techpark_uq.model.dto.SolicitudColaDTO;
import com.techpark.techpark_uq.service.ColaVirtualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/colas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ColaVirtualController {
    
    private final ColaVirtualService colaVirtualService;
    
    /**
     * Unirse a la cola virtual de una atracción
     */
    @PostMapping("/unirse")
    public ResponseEntity<ApiResponseDTO<RespuestaColaDTO>> unirseACola(
            @Valid @RequestBody SolicitudColaDTO solicitud,
            HttpServletRequest request) {
        
        RespuestaColaDTO respuesta = colaVirtualService.unirseACola(solicitud);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            respuesta,
            "Te has unido a la cola correctamente",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener el siguiente visitante en la cola (para operador)
     */
    @GetMapping("/siguiente/{atraccionId}/{operadorId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> siguienteEnCola(
            @PathVariable Long atraccionId,
            @PathVariable Long operadorId,
            HttpServletRequest request) {
        
        ColaVirtualService.ElementoCola siguiente = colaVirtualService.siguienteEnCola(atraccionId, operadorId);
        
        Map<String, Object> respuesta = Map.of(
            "visitanteId", siguiente.getVisitanteId(),
            "nombreVisitante", siguiente.getNombreVisitante(),
            "tipoTicket", siguiente.getTipoTicket(),
            "horaIngreso", siguiente.getHoraIngreso()
        );
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            respuesta,
            "Visitante atendido correctamente",
            request.getRequestURI()
        ));
    }
    
    /**
     * Cancelar la posición en cola
     */
    @DeleteMapping("/cancelar/{visitanteId}/{atraccionId}")
    public ResponseEntity<ApiResponseDTO<Boolean>> cancelarCola(
            @PathVariable Long visitanteId,
            @PathVariable Long atraccionId,
            HttpServletRequest request) {
        
        boolean cancelado = colaVirtualService.cancelarCola(visitanteId, atraccionId);
        
        if (cancelado) {
            return ResponseEntity.ok(ApiResponseDTO.success(
                true,
                "Has cancelado tu posición en la cola",
                request.getRequestURI()
            ));
        } else {
            return ResponseEntity.ok(ApiResponseDTO.success(
                false,
                "No se encontró una posición activa en la cola",
                request.getRequestURI()
            ));
        }
    }
    
    /**
     * Obtener estado de la cola de una atracción
     */
    @GetMapping("/estado/{atraccionId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerEstadoCola(
            @PathVariable Long atraccionId,
            HttpServletRequest request) {
        
        Map<String, Object> estado = colaVirtualService.obtenerEstadoCola(atraccionId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            estado,
            "Estado de cola obtenido correctamente",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener posición actual de un visitante
     */
    @GetMapping("/posicion/{visitanteId}/{atraccionId}")
    public ResponseEntity<ApiResponseDTO<Integer>> obtenerPosicion(
            @PathVariable Long visitanteId,
            @PathVariable Long atraccionId,
            HttpServletRequest request) {
        
        Integer posicion = colaVirtualService.obtenerPosicionVisitante(visitanteId, atraccionId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            posicion,
            posicion != null ? "Posición obtenida" : "No estás en la cola",
            request.getRequestURI()
        ));
    }
    
    /**
     * Obtener todas las colas activas
     */
    @GetMapping("/todas")
    public ResponseEntity<ApiResponseDTO<Map<Long, Map<String, Object>>>> obtenerTodasLasColas(
            HttpServletRequest request) {
        
        Map<Long, Map<String, Object>> todasLasColas = colaVirtualService.obtenerTodasLasColas();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            todasLasColas,
            "Colas activas obtenidas",
            request.getRequestURI()
        ));
    }
}
