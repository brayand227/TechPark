package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroVisitanteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "La edad es obligatoria")
    @Min(0)
    private Integer edad;

    @NotNull(message = "La estatura es obligatoria")
    @DecimalMin("0.5")
    private Double estatura;

    @NotBlank(message = "El tipo de ticket es obligatorio")
    private String tipoTicket; // GENERAL, FAMILIAR, FAST_PASS

    private String fotoUrl; // Opcional
}