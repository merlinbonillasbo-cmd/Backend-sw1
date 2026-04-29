// common/NodeNotFoundException.java
package com.workflow.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lanzado cuando un nodo del diagrama no existe en la definición de política.
 * Equivalente al caso en que FlowNodeStateManagerImpl no encuentra estado siguiente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(String nodeId) {
        super("Nodo no encontrado en la definición: " + nodeId);
    }

    public NodeNotFoundException(String nodeId, String policyId) {
        super("Nodo '" + nodeId + "' no encontrado en la política: " + policyId);
    }
}
