package net.es.funfacts.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Peering {
    private String router;
    private String int_name;
    private String address;
    private String alias;
    private String peer_addr;
    private Integer mbps;
    private Integer remote_as;
    private String port;

}
