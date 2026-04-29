// model/embedded/SearchIndex.java
package com.workflow.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndex {

    @Indexed
    private String rut;

    @Indexed
    private String codigoCaso;
}
