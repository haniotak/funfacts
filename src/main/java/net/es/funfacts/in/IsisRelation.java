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
public class IsisRelation {
    private String a;
    private String z;
    private String a_port;
    private String z_port;
    private String a_ifce;
    private String z_ifce;
    private String a_addr;
    private String z_addr;
    private String admin;
    private Integer mbps;
    private Integer latency;
    private Integer isis_cost;
}