package net.es.funfacts.cont;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.funfacts.in.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfoCard {
    private Device device;
    private Map<String, List<PortDetails>> ports;
    private Map<String, List<IfceDetails>> ifces;
    private List<Peering> peerings;
    private List<Maintenance> maintenances;
    private Map<String, Circuit> circuits;
    private List<String> syslog;

}
