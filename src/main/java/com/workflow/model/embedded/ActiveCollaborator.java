// model/embedded/ActiveCollaborator.java
package com.workflow.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveCollaborator {

    private String userId;
    private String socketId;
    private Instant joinedAt;
}
