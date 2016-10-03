package net.es.funfacts.pop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.funfacts.in.*;
import net.es.funfacts.prop.InputConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Data
public class Input {
    @Autowired
    private InputConfig inputConfig;

    private Map<String, Circuit> circuits;

    private List<Device> devices;

    private Map<String, List<String>> hubs;

    private Map<String, Map<String, List<IfceDetails>>> ifces;

    private List<IpAddrDetails> ip_addrs;

    private List<IsisRelation> isis;

    private List<MacDetails> mac;

    private List<Maintenance> maintenance;

    private List<OscarsDetails> oscars;

    private List<Peering> peerings;

    private Map<String, Position> positions;

    private Map<String, Map<String, List<PortDetails>>> ports;

    private Map<String, List<String>> syslog;

    private List<VlanDetails> vlans;

    @PostConstruct
    public void startup() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File circuitsFile = inputConfig.getCircuits();
        circuits = mapper.readValue(circuitsFile, new TypeReference<Map<String, Circuit>>() {});
        log.info("circuits imported: " + circuits.size());

        File devicesFile = inputConfig.getDevices();
        devices = mapper.readValue(devicesFile, new TypeReference<List<Device>>() {});
        log.info("devices imported: " + devices.size());


        File hubsFile = inputConfig.getHubs();
        hubs = mapper.readValue(hubsFile, new TypeReference< Map<String, List<String>>>() {});
        log.info("hubs imported: " + hubs.size());

        File ifcesFile = inputConfig.getIfces();
        ifces = mapper.readValue(ifcesFile, new TypeReference< Map<String, Map<String, List<IfceDetails>>>>() {});
        log.info("ifces imported for devices: " + ifces.size());


        File ipaddrsFile = inputConfig.getIp_addrs();
        ip_addrs = mapper.readValue(ipaddrsFile, new TypeReference< List<IpAddrDetails>>() {});
        log.info("ip_addrs imported: " + ip_addrs.size());

        File isisFile = inputConfig.getIsis();
        isis = mapper.readValue(isisFile, new TypeReference< List<IsisRelation>>() {});
        log.info("is-is relationships imported: " + isis.size());

        File macFile = inputConfig.getMac();
        mac = mapper.readValue(macFile, new TypeReference< List<MacDetails>>() {});
        log.info("MAC addresses imported: " + mac.size());

        File maintFile = inputConfig.getMaintenance();
        maintenance = mapper.readValue(maintFile, new TypeReference< List<Maintenance>>() {});
        log.info("Maintenance events imported: " + maintenance.size());

        File oscarsFile = inputConfig.getOscars();
        oscars = mapper.readValue(oscarsFile, new TypeReference< List<OscarsDetails>>() {});
        log.info("OSCARS VCs imported: " + oscars.size());

        File peeringsFile = inputConfig.getPeerings();
        peerings = mapper.readValue(peeringsFile, new TypeReference<List<Peering>>() {});
        log.info("peerings imported: " + peerings.size());

        File portsFile = inputConfig.getPorts();
        ports = mapper.readValue(portsFile, new TypeReference< Map<String, Map<String, List<PortDetails>>>>() {});
        log.info("ports imported for devices: " + ports.size());


        File positionsFile = inputConfig.getPositions();
        positions = mapper.readValue(positionsFile, new TypeReference< Map<String, Position>>() {});
        log.info("positions imported for devices: " + positions.size());


        File syslogFile = inputConfig.getSyslog();
        syslog = mapper.readValue(syslogFile, new TypeReference< Map<String, List<String>>>() {});
        log.info("syslog imported for devices: " + syslog.size());

        File vlansFile = inputConfig.getVlan();
        vlans = mapper.readValue(vlansFile, new TypeReference<List<VlanDetails>>() {});
        log.info("vlans imported: " + vlans.size());

    }

}
