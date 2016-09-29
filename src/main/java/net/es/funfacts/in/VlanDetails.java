package net.es.funfacts.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VlanDetails {
    private String router;
    private String int_name;
    private String alias;
    private Integer mbps;
    private String address;
    private Integer vlan_id;

}
