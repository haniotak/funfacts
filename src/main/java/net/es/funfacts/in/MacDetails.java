package net.es.funfacts.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MacDetails {
    private String router;
    private String mac;
    private String int_name;
    private String port;
    private String address;
}
