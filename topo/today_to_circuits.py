#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/circuits.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    circ_phy = data["today"]["circ_phy"]
    ipv4nets = data["today"]["ipv4net"]
    circuits = get_circuits(circ_phy=circ_phy, ipv4nets=ipv4nets)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(circuits, outfile, indent=2)


def get_circuits(circ_phy=None, ipv4nets=None):
    pp = pprint.PrettyPrinter(indent=4)

    ipv4_by_circuit_name = {}
    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]
            if "circuit" in ipv4net_info.keys():
                circuit_names = ipv4net_info["circuit"].keys()
                mbps = ipv4net_info["high_speed"]
                int_name = ipv4net_info["int_name"]
                alias = ipv4net_info["alias"]
                admin = ipv4net_info["admin"]
                address, address_info = topo.filter_address_info(ipv4net_info=ipv4net_info)
                if not address:
                    continue
                for circuit_name in circuit_names:
                    entry = {
                        "mbps": mbps,
                        "router": router,
                        "int_name": int_name,
                        "alias": alias,
                        "admin": admin,
                        "address": address
                    }
                    port = topo.guess_port(ipv4net_info)
                    if port:
                        entry["port"] = port

                    if circuit_name not in ipv4_by_circuit_name.keys():
                        ipv4_by_circuit_name[circuit_name] = [entry]
                    else:
                        ipv4_by_circuit_name[circuit_name].append(entry)

    circuits = {}

    for circuit_name in circ_phy.keys():
        circuit_info = circ_phy[circuit_name]
        carrier = circuit_info["Carrier"]
        description = circuit_info["description"]
        service_type = circuit_info["service_type"]
        circuit_names = [circuit_name]
        if description != circuit_name:
            circuit_names.append(description)
        if service_type != circuit_name:
            circuit_names.append(service_type)

        c_entry = {
            "carrier": carrier,
            "names": circuit_names
        }

        if circuit_name in ipv4_by_circuit_name.keys():
            c_entry["ifces"] = ipv4_by_circuit_name[circuit_name]
            circuits[circuit_name] = c_entry

    return circuits


if __name__ == '__main__':
    main()
