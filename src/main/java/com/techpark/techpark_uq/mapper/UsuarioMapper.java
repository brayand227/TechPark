package com.techpark.techpark_uq.mapper;



import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.model.entity.Visitante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    
    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);
    
    UsuarioDTO toDto(Usuario usuario);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "rol", constant = "VISITANTE")
    Visitante toEntity(RegistroVisitanteRequest request);
}
