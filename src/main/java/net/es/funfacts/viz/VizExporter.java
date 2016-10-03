package net.es.funfacts.viz;

import edu.mines.jtk.awt.ColorMap;
import lombok.extern.slf4j.Slf4j;
import net.es.funfacts.in.IsisRelation;
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
