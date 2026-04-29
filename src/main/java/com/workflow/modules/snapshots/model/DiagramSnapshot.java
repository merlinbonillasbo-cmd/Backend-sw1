// modules/snapshots/model/DiagramSnapshot.java
package com.workflow.modules.snapshots.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "snapshots_diagrama")
public class DiagramSnapshot {

    @Id
    private String id;

    private String idPolitica;

    private long version;

    private Map<String, Object> datosSnapshot;

    private String guardadoPor;

    @CreatedDate
    private Instant fechaCreacion;
}
