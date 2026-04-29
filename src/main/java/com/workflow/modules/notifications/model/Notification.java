// modules/notifications/model/Notification.java
package com.workflow.modules.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificaciones")
public class Notification {

    @Id
    private String id;

    private String idUsuario;

    private String titulo;

    private String mensaje;

    private String tipo;

    private String idInstancia;

    private String idTarea;

    @Builder.Default
    private boolean leido = false;

    @Indexed(expireAfterSeconds = 2592000)
    @CreatedDate
    private Instant fechaCreacion;
}
