package net.es.funfacts.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Circuit {
    private String carrier;
    private List<String> names;
    private List<CircuitIfce> ifces;

}
