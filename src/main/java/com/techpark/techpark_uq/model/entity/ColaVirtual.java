package com.techpark.techpark_uq.model.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "colas_virtuales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColaVirtual {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "visitante_id")
    private Visitante visitante;
    
    @ManyToOne
    @JoinColumn(name = "atraccion_id")
    private Atraccion atraccion;
    
    private Integer prioridad;  // 1 = Fast-Pass, 2 = General
    
    private LocalDateTime horaIngresoCola;
    private Boolean atendido = false;
    private Integer posicion;  // Posición en la cola
}