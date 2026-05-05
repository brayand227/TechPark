package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.AtraccionDTO;
import com.techpark.techpark_uq.service.AtraccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/atracciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AtraccionController {

    private final AtraccionService atraccionService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarTodas(
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarTodas();
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones listadas exitosamente",
            request.getRequestURI()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AtraccionDTO>> obtenerPorId(
            @PathVariable Long id,
            HttpServletRequest request) {
        AtraccionDTO atraccion = atraccionService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atraccion,
            "Atracción encontrada",
            request.getRequestURI()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<AtraccionDTO>> crear(
            @Valid @RequestBody AtraccionDTO atraccionDTO,
            HttpServletRequest request) {
        AtraccionDTO nueva = atraccionService.crear(atraccionDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(
            nueva,
            "Atracción creada exitosamente",
            request.getRequestURI()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AtraccionDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtraccionDTO atraccionDTO,
            HttpServletRequest request) {
        AtraccionDTO actualizada = atraccionService.actualizar(id, atraccionDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(
            actualizada,
            "Atracción actualizada exitosamente",
            request.getRequestURI()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(
            @PathVariable Long id,
            HttpServletRequest request) {
        atraccionService.eliminar(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Atracción eliminada exitosamente",
            request.getRequestURI()
        ));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarPorEstado(
            @PathVariable String estado,
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarPorEstado(estado);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones filtradas por estado",
            request.getRequestURI()
        ));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarPorTipo(
            @PathVariable String tipo,
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarPorTipo(tipo);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones filtradas por tipo",
            request.getRequestURI()
        ));
    }

    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarPorZona(
            @PathVariable Long zonaId,
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarPorZona(zonaId);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones filtradas por zona",
            request.getRequestURI()
        ));
    }
}