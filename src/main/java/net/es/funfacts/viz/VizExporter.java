package net.es.funfacts.viz;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.bench.ArrayListBench;
import lombok.extern.slf4j.Slf4j;
import net.es.funfacts.in.*;
import net.es.funfacts.pop.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.List;

@Component
@Slf4j
public class VizExporter {

    @Autowired
    private Input input;

    public VizGraph isisGraph() {
        List<IsisRelation> isisRelations = input.getIsis();
        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();

        List<String> routers = new ArrayList<>();
        Set<String> seenAddrs = new HashSet<>();
        List<IsisRelation> seenBothSides = new ArrayList<>();

        for (IsisRelation isis : isisRelations) {
            String a_addr = isis.getA_addr();
            String z_addr = isis.getZ_addr();

            if (seenAddrs.contains(a_addr) || seenAddrs.contains(z_addr)) {
                // we have seen either A or Z address before.
                seenBothSides.add(isis);
            }
            seenAddrs.add(a_addr);
            seenAddrs.add(z_addr);
        }

        for (IsisRelation isis : seenBothSides) {
            String a = isis.getA();
            String z = isis.getZ();
            String id = isis.getA_addr();
            // log.info("setting id: "+id);
            String title = a + ":" + isis.getA_port() + " -- " + z + ":" + isis.getZ_port();
            if (!routers.contains(a)) {
                routers.add(a);
            }
            if (!routers.contains(z)) {
                routers.add(z);
            }
            VizEdge ve = VizEdge.builder()
                    .from(a).to(z).title(title).label("").value(1)
                    .id(id)
                    .arrows(null).arrowStrikethrough(false).color(null)
                    .build();
            g.getEdges().add(ve);
        }

        for (String router : routers) {
            this.makeNode(router, g);
        }

        return g;

    }

    public HighlightResult highlightCircuits(String query) {
        HighlightResult result = HighlightResult.builder().edgeIds(new ArrayList<>()).nodeIds(new ArrayList<>()).build();
        if (query == null || query.equals("")) {
            return result;
        }

        Set<Circuit> circuits = new HashSet<>();
        Set<String> devices = new HashSet<>();
        Set<String> addresses = new HashSet<>();

        for (String circuitName : input.getCircuits().keySet()) {
            boolean found = false;
            Circuit c = input.getCircuits().get(circuitName);

            if (circuitName.toLowerCase().contains(query)) {
                found = true;
            }

            for (String name : c.getNames()) {
                if (name.toLowerCase().contains(query)) {
                    found = true;
                }
            }
            if (c.getCarrier().toLowerCase().contains(query)) {
                found = true;

            }
            if (found) {
                circuits.add(c);
            }
        }
        for (Circuit c: circuits) {
            List<CircuitIfce> cIfces = c.getIfces();
            for (CircuitIfce cIfce : cIfces) {
                devices.add(cIfce.getRouter());
                addresses.add(cIfce.getAddress());
            }
        }
        result.getEdgeIds().addAll(addresses);
        result.getNodeIds().addAll(devices);
        return result;
    }




    public HighlightResult highlightIfces(String query) {
        HighlightResult result = HighlightResult.builder().edgeIds(new ArrayList<>()).nodeIds(new ArrayList<>()).build();
        if (query == null || query.equals("")) {
            return result;
        }

        String[] parts = query.split(":");

        Set<String> addresses = new HashSet<>();
        Set<String> devices = new HashSet<>();

        List<String> routers = new ArrayList<>();
        String ifce;

        if (parts.length == 0) {
            return result;

        } else if (parts.length == 1) {
            ifce = parts[0];
            routers.addAll(input.getIfces().keySet());
        } else if (parts.length == 2) {
            if (!input.getIfces().keySet().contains(parts[0])) {
                return result;
            }
            routers.add(parts[0]);
            ifce = parts[1];
        } else {
            return result;

        }

        for (String router : routers) {
            for (String ifceName : input.getIfces().get(router).keySet()) {
                for (IfceDetails details : input.getIfces().get(router).get(ifceName)) {
                    boolean found = false;

                    if (ifceName.toLowerCase().contains(ifce)) {
                        found = true;
                    }
                    if (details.getIp_name() != null && details.getIp_name().toLowerCase().contains(query)) {
                        found = true;
                    }
                    if (details.getAddress() != null && details.getAddress().contains(query)) {
                        found = true;
                    }
                    if (details.getAlias().toLowerCase().contains(query)) {
                        found = true;
                    }

                    if (found) {
                        devices.add(router);
                        if (details.getAddress() != null) {
                            addresses.add(details.getAddress());
                        }
                    }
                }
            }
        }

        Set<String> isisAddrs = new HashSet<>();
        for (String address : addresses) {
            for (IsisRelation isis : input.getIsis()) {
                if (address.equals(isis.getA_addr())) {
                    isisAddrs.add(isis.getZ_addr());
                } else if (address.equals(isis.getZ_addr())) {
                    isisAddrs.add(isis.getA_addr());
                }
            }
        }
        addresses.addAll(isisAddrs);

        for (String router : routers) {
            for (String portName : input.getPorts().get(router).keySet()) {
                for (PortDetails pd : input.getPorts().get(router).get(portName)) {
                    boolean found = false;

                    if (portName.toLowerCase().contains(ifce)) {
                        found = true;
                    }
                    if (pd.getInt_name().toLowerCase().contains(query)) {
                        found = true;
                    }
                    if (pd.getAlias().toLowerCase().contains(query)) {
                        found = true;
                    }
                    if (found) {
                        devices.add(router);
                    }
                }

            }
        }


        result.getEdgeIds().addAll(addresses);
        result.getNodeIds().addAll(devices);

        return result;
    }


    public VizGraph circuitGraph() {

        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();

        return g;

    }


    private void makeNode(String node, VizGraph g) {

        Map<String, List<String>> hubs = input.getHubs();
        String hub = null;
        for (String h : hubs.keySet()) {
            if (hubs.get(h).contains(node)) {
                hub = h;
            }
        }

        VizNode n = VizNode.builder().id(node).label(node).title(node).value(1).build();
        if (hub != null) {
            n.setGroup(hub);
        }

        if (input.getPositions().keySet().contains(node)) {
            n.setFixed(new HashMap<>());
            n.getFixed().put("x", true);
            n.getFixed().put("y", true);
            n.setX(input.getPositions().get(node).getX());
            n.setY(input.getPositions().get(node).getY());
        }

        g.getNodes().add(n);
    }

    private String toWeb(Color c) {
        String rgb = Integer.toHexString(c.getRGB());
        rgb = "#" + rgb.substring(2, rgb.length());
        return rgb;
    }

    private String shorten(Double mbps) {

        BigDecimal bd = new BigDecimal(mbps);
        bd = bd.round(new MathContext(3));
        double rounded = bd.doubleValue();

        if (mbps < 1000.0) {
            return rounded + "M";
        } else if (mbps < 1000.0 * 1000) {
            return rounded / 1000 + "G";
        } else if (mbps < 1000.0 * 1000000) {
            return rounded / 1000000 + "T";
        } else if (mbps < 1000.0 * 1000000000) {
            return rounded / 1000000000 + "P";
        } else {
            return ">1000P";
        }


    }

}
