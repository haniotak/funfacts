#!/usr/bin/env python
# encoding: utf-8

import json
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/peerings.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    ipv4nets = data["today"]["ipv4net"]
    peerings = get_peerings(ipv4nets=ipv4nets)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(peerings, outfile, indent=2)


def get_peerings(ipv4nets=None):
    peerings = []

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]

            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]
            alias = ipv4net_info["alias"]
            address, address_info = topo.filter_address_info(ipv4net_info=ipv4net_info)
            if not address:
                continue

            if "bgp_peers" in address_info.keys():
                for peer_ip in address_info["bgp_peers"].keys():
                    remote_as = address_info["bgp_peers"][peer_ip]["remote_as"]
                    entry = {
                        "router": router,
                        "mbps": mbps,
                        "int_name": int_name,
                        "address": address,
                        "alias": alias,
                        "peer_addr": peer_ip,
                        "remote_as": remote_as
                    }

                    port = topo.guess_port(ipv4net_info)
                    if port:
                        entry["port"] = port

                    peerings.append(entry)

    return peerings


if __name__ == '__main__':
    main()
