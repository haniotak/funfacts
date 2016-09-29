package net.es.funfacts.viz;

import edu.mines.jtk.awt.ColorMap;
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
public class VizExporter {

    @Autowired
    private Input input;

    public VizGraph isisGraph() {
        List<IsisRelation> isisRelations = input.getIsis();
        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();

        List<String> seenNodes = new ArrayList<>();
        for (IsisRelation isis : isisRelations) {
            if (!seenNodes.contains(isis.getA())) {
                seenNodes.add(isis.getA());
            }
            if (!seenNodes.contains(isis.getZ())) {
                seenNodes.add(isis.getZ());
            }
        }

        return g;

    }

    public VizGraph circuitGraph() {

        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();

        return g;

    }


    private void makeNode(String node, List<String> seenNodes, Map<String, Double> nodeIngresses, VizGraph g) {
        if (seenNodes.contains(node)) {
            return;
        }
        seenNodes.add(node);
        Double ingress = 0.0;
        if (nodeIngresses.keySet().contains(node)) {
            ingress = nodeIngresses.get(node);
        }
        String title = shorten(ingress);

        VizNode n = VizNode.builder().id(node).label(node).title(title).value(ingress.intValue()).build();
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
