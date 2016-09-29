#!/usr/bin/env python
# encoding: utf-8

import json
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/mac.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    ipv4nets = data["today"]["ipv4net"]
    mac_ipv4net = get_mac_ipv4net(ipv4nets=ipv4nets)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(mac_ipv4net, outfile, indent=2)


def get_mac_ipv4net(ipv4nets=None):
    mac_ipv4net = []

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]

            int_name = ipv4net_info["int_name"]

            if "mac" in ipv4net_info.keys():
                mac = ipv4net_info["mac"]
                (address, address_info) = topo.filter_address_info(ipv4net_info=ipv4net_info)
                if not address:
                    continue
                mac_entry = {
                    "router": router,
                    "int_name": int_name,
                    "address": address,
                    "mac": mac
                }
                port = topo.guess_port(ipv4net_info)

                if port:
                    mac_entry["port"] = port

                mac_ipv4net.append(mac_entry)

    return mac_ipv4net


if __name__ == '__main__':
    main()
