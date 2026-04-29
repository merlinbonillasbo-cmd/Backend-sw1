package com.workflow.modules.forms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinition {

    private String id;
    private String name;
    private String label;
    private String ayuda;
    private String type;
    private boolean required;
    private Object defaultValue;
    private Map<String, Object> validations;
    private Map<String, Object> options;

}
