package com.workflow.modules.forms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "form_contracts")
public class FormContract {

    @Id
    private String id;

    private String name;

    private String description;

    private String workflowDefinitionId;

    private String nodeId;

    private List<FieldDefinition> fields;

}
