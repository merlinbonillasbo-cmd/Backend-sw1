package com.workflow.modules.quejas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quejas")
public class Queja {

    @Id
    private String id;

    private String tipo; // TECNICA, ADMINISTRATIVA, CLIENTE, OTRA

    private String descripcion;

    private String idUsuario;

    private String nombreUsuario;

    private String departamentoCodigo;

    private String departamentoNombre;

    @CreatedDate
    private Instant fechaCreacion;
}
