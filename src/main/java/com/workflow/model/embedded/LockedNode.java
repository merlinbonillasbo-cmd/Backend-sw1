// model/embedded/LockedNode.java
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
public class LockedNode {

    private String nodeId;
    private String lockedBy;
    private Instant lockedAt;
}
