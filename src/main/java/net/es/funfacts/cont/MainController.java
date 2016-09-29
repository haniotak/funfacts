package net.es.funfacts.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.funfacts.pop.Input;
import net.es.funfacts.viz.VizExporter;
import net.es.funfacts.viz.VizGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
        String graph_url = "/graphs/isis";
        model.addAttribute("graph_url", graph_url);
        String highlight_url = "/highlights";
        model.addAttribute("highlight_url", highlight_url);
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

}
