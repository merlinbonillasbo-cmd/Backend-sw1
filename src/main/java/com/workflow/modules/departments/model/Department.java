package com.workflow.modules.departments.model;

import com.workflow.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departamentos")
public class Department {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private String nombre;

    private String descripcion;

    private String color;

    private int orden;

    /** Rol que heredan todos los usuarios asignados a este departamento */
    private UserRole rolAsignado;

    @Builder.Default
    private boolean activo = true;

    private List<String> idsMiembros;

    @Version
    private Long version;
}
