// common/WorkflowException.java
package com.workflow.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción base del motor de workflow.
 * Inspirado en SActivityExecutionException de BonitaSoft, adaptado para Spring Boot 3.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class WorkflowException extends RuntimeException {

    public WorkflowException(String message) {
        super(message);
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
