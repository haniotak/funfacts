#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/ports.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    ipv4nets = data["today"]["ipv4net"]
    ports_by_rtr = get_ports_by_rtr(ipv4nets=ipv4nets)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(ports_by_rtr, outfile, indent=2)


def get_ports_by_rtr(ipv4nets=None):
    pp = pprint.PrettyPrinter(indent=4)
    ports_by_rtr = {}

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]

            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]
            alias = ipv4net_info["alias"]

            port = topo.guess_port(ipv4net_info)

            if port:
                if router not in ports_by_rtr.keys():
                    ports_by_rtr[router] = {}
                if port not in ports_by_rtr[router].keys():
                    ports_by_rtr[router][port] = []

                entry = {
                    "router": router,
                    "int_name": int_name,
                    "mbps": mbps,
                    "alias": alias
                }

                ports_by_rtr[router][port].append(entry)

    return ports_by_rtr


if __name__ == '__main__':
    main()
