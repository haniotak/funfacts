package net.es.funfacts.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.funfacts.in.*;
import net.es.funfacts.pop.Input;
import net.es.funfacts.viz.HighlightResult;
import net.es.funfacts.viz.VizExporter;
import net.es.funfacts.viz.VizGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.tags.form.OptionsTag;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@EnableAutoConfiguration
public class MainController {
    @Autowired
    private VizExporter vizExporter;

    @Autowired
    private Input input;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("user requested a strResource which didn't exist", ex);
    }

    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }


    @RequestMapping(value = "/graphs/{classifier}", method = RequestMethod.GET)
    @ResponseBody
    public VizGraph viz_for(@PathVariable String classifier) {
        if (classifier.equals("isis")) {
            return vizExporter.isisGraph();

        } else if (classifier.equals("circuit")) {

            return vizExporter.circuitGraph();
        } else {
            throw new NoSuchElementException("bad classifier " + classifier);
        }
    }

    @RequestMapping(value = "/info/devices", method = RequestMethod.GET)
    @ResponseBody
    public List<String> info_devices() {
        List<String> result = new ArrayList<>();

        input.getDevices().forEach(d -> {
            result.add(d.getName());
        });
        return result;

    }
    @RequestMapping(value = "/highlight/ifces", method = RequestMethod.GET)
    @ResponseBody
    public HighlightResult highlight_ifces(@RequestParam("query") String query) {
        return vizExporter.highlightIfces(query);
    }

    @RequestMapping(value = "/highlight/circuits", method = RequestMethod.GET)
    @ResponseBody
    public HighlightResult highlight_circuits(@RequestParam("query") String query) {
        return vizExporter.highlightCircuits(query);
    }

    @RequestMapping(value = "/node_info/{node_id}", method = RequestMethod.GET)
    @ResponseBody
    public NodeInfoCard node_info(@PathVariable String node_id) {
        NodeInfoCard result = NodeInfoCard.builder()
                .circuits(new HashMap<>())
                .build();

        result.setDevice(input.getDevices().stream()
                .filter(d -> d.getName().equals(node_id))
                .findAny().orElseThrow(NoSuchElementException::new));

        result.setMaintenances(input.getMaintenance().stream()
                .filter(m -> m.getRouter() != null && m.getRouter().equals(node_id))
                .collect(Collectors.toList()));

        result.setPorts(input.getPorts().get(node_id));

        for (String circuitName : input.getCircuits().keySet()) {
            boolean add = false;
            Circuit c = input.getCircuits().get(circuitName);
            for (CircuitIfce ci : c.getIfces()) {
                if (ci.getRouter().equals(node_id)) {
                    add = true;
                }
            }
            if (add) {
                result.getCircuits().put(circuitName, c);
            }
        }
        result.setPeerings(input.getPeerings().stream()
                .filter(p -> p.getRouter().equals(node_id))
                .collect(Collectors.toList()));
        result.setIfces(input.getIfces().get(node_id));
        result.setSyslog(input.getSyslog().get(node_id));

        return result;
    }

    @RequestMapping(value = "/edge_info/{ip_addr:.+}", method = RequestMethod.GET)
    @ResponseBody
    public EdgeInfoCard edge_info(@PathVariable String ip_addr) {
        EdgeInfoCard result = EdgeInfoCard.builder()
                .display(true)
                .ifces(new HashMap<>())
                .ports(new HashMap<>())
                .circuits(new HashMap<>())
                .build();

        String the_router = null;
        String the_ifce = null;
        String the_port = null;

        for (String router : input.getIfces().keySet()) {
            Map<String, List<IfceDetails>> ifces = input.getIfces().get(router);
            for (String int_name : ifces.keySet()) {
                for (IfceDetails ifce : ifces.get(int_name)) {
                    if (ifce.getAddress() != null && ifce.getAddress().equals(ip_addr)) {
                        the_router = router;
                        the_ifce = int_name;
                        the_port = ifce.getPort();
                    }
                }
            }
        }

        if (the_router == null) {
            log.info("cannot find address " + ip_addr);
            result.setDisplay(false);
            return result;
        } else {
            log.info("found " + the_router + ":" + the_ifce);

        }


        Map<String, String> ifces = new HashMap<>();
        Map<String, String> ports = new HashMap<>();
        ifces.put(the_router, the_ifce);
        if (the_port != null) {
            ports.put(the_router, the_port);
        }

        List<String> circuits = new ArrayList<>();

        for (String circuitName : input.getCircuits().keySet()) {
            boolean add = false;
            Circuit c = input.getCircuits().get(circuitName);
            for (CircuitIfce ci : c.getIfces()) {
                if (ci.getRouter().equals(the_router) && ci.getInt_name().equals(the_ifce)) {
                    add = true;
                }
            }
            if (add) {
                circuits.add(circuitName);
                for (CircuitIfce ci : c.getIfces()) {
                    ifces.put(ci.getRouter(), ci.getInt_name());
                    ports.put(ci.getRouter(), ci.getPort());
                }
                result.getCircuits().put(circuitName, c);
            }
        }

        for (String router : ifces.keySet()) {
            String ifce_name = ifces.get(router);
            List<IfceDetails> ifceDetails = input.getIfces().get(router).get(ifce_name);
            String out_name = router + ":" + ifce_name;
            result.getIfces().put(out_name, ifceDetails);
        }

        for (String router : ports.keySet()) {
            String port_name = ports.get(router);
            List<PortDetails> portDetails = input.getPorts().get(router).get(port_name);
            String out_name = router + ":" + port_name;
            result.getPorts().put(out_name, portDetails);
        }

        List<Maintenance> maintenances = new ArrayList<>();
        for (Maintenance m : input.getMaintenance()) {
            if (m.getCircuit() != null && circuits.contains(m.getCircuit())) {
                maintenances.add(m);
            } else if (m.getRouter() != null && m.getPort() != null) {
                if (ports.keySet().contains(m.getRouter())) {
                    if (ports.get(m.getRouter()).equals(m.getPort())) {
                        maintenances.add(m);
                    }
                }
            }
        }
        result.setMaintenances(maintenances);

        return result;

    }

}
