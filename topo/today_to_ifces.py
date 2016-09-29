#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/ifces.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    ipv4nets = data["today"]["ipv4net"]
    ifces_by_rtr = get_ports_by_rtr(ipv4nets=ipv4nets)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(ifces_by_rtr, outfile, indent=2)


def get_ports_by_rtr(ipv4nets=None):
    pp = pprint.PrettyPrinter(indent=4)
    ifces_by_rtr = {}

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]

            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]
            alias = ipv4net_info["alias"]

            if router not in ifces_by_rtr.keys():
                ifces_by_rtr[router] = {}

            if int_name not in ifces_by_rtr[router].keys():
                ifces_by_rtr[router][int_name] = []
            entry = {
                "mbps": mbps,
                "alias": alias
            }

            if "VLAN" in ipv4net_info.keys():
                entry["vlan"] = ipv4net_info["VLAN"]

            port = topo.guess_port(ipv4net_info)
            if port:
                entry["port"] = port

            address, address_info = topo.filter_address_info(ipv4net_info=ipv4net_info)
            if address:
                entry["address"] = address
                ip_name = ipv4net_info["ip_addr"][address]["ip_name"]
                entry["ip_name"] = ip_name



            ifces_by_rtr[router][int_name].append(entry)

    return ifces_by_rtr


if __name__ == '__main__':
    main()
