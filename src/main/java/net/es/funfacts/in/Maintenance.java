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
public class Maintenance {
    private String router;
    private String port;
    private String circuit;
    private String scope;
    private String type;
    private List<String> tickets;

}
