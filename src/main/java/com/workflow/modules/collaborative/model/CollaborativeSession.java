// modules/collaborative/model/CollaborativeSession.java
package com.workflow.modules.collaborative.model;

import com.workflow.model.embedded.ActiveCollaborator;
import com.workflow.model.embedded.LockedNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sesiones_colaborativas")
public class CollaborativeSession {

    @Id
    private String id;

    @Indexed(unique = true)
    private String idPolitica;

    private List<ActiveCollaborator> usuariosActivos;

    private List<LockedNode> nodosBloqueados;

    @Indexed(expireAfterSeconds = 300)
    @LastModifiedDate
    private Instant fechaActualizacion;
}
