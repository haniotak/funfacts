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
public class IpAddrDetails {
    private String int_name;
    private String admin;
    private Integer mbps;
    private String alias;
    private String mac;
    private String address;
    private String router;
    private String port;
    private List<String> circuit;
    private List<String> oscars;
    private List<IpAddrBgp> bgp_peers;
}