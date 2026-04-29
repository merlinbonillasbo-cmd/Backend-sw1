// modules/roles/model/RolePermission.java
package com.workflow.modules.roles.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles_permisos")
public class RolePermission {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombreRol;

    private List<String> permisos;

    @Builder.Default
    private boolean esRolSistema = false;
}
