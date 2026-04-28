package com.techpark.techpark_uq.repository;


import com.techpark.techpark_uq.model.entity.ColaVirtual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ColaVirtualRepository extends JpaRepository<ColaVirtual, Long> {
    
    List<ColaVirtual> findByAtraccionIdAndAtendidoFalseOrderByPrioridadAscHoraIngresoColaAsc(Long atraccionId);
    
    @Query("SELECT COUNT(c) FROM ColaVirtual c WHERE c.atraccion.id = :atraccionId AND c.atendido = false")
    Integer contarPersonasEnCola(Long atraccionId);
}