package net.es.funfacts.prop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "input")

public class InputConfig {
    private File circuits;
    private File devices;
    private File hubs;
    private File ifces;
    private File ip_addrs;
    private File isis;
    private File mac;
    private File maintenance;
    private File oscars;
    private File peerings;
    private File ports;
    private File syslog;
    private File vlan;

}
