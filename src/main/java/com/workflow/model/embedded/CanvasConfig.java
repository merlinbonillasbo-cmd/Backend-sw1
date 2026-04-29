// model/embedded/CanvasConfig.java
package com.workflow.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasConfig {

    private double zoom;
    private double panX;
    private double panY;
    private String background;
}
