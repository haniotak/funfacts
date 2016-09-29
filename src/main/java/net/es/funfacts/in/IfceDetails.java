package net.es.funfacts.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IfceDetails {
    private Integer mbps;
    private Integer vlan;
    private String ip_name;
    private String address;
    private String alias;
    private String port;
}
