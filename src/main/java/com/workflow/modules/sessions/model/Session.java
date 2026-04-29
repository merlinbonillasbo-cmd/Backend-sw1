// modules/sessions/model/Session.java
package com.workflow.modules.sessions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sesiones")
public class Session {

    @Id
    private String id;

    private String idUsuario;

    @Indexed(unique = true)
    private String tokenAcceso;

    private String tokenRefresh;

    private String idSocket;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiraEn;
}
