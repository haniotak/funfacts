package net.es.funfacts.viz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VizNode {
    String id;
    String label;
    Integer value;
    String title;

}
