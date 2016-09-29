#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/ip_addrs.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    ipv4nets = data["today"]["ipv4net"]
    ip_addrs = get_ip_addrs(ipv4nets=ipv4nets)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(ip_addrs, outfile, indent=2)


def get_ip_addrs(ipv4nets=None):
    pp = pprint.PrettyPrinter(indent=4)
    ip_addrs = []

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]

            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]
            alias = ipv4net_info["alias"]
            admin = ipv4net_info["admin"]

            address, address_info = topo.filter_address_info(ipv4net_info=ipv4net_info)
            if not address:
                continue

            ip_entry = {
                "router": router,
                "mbps": mbps,
                "int_name": int_name,
                "admin": admin,
                "address": address,
                "alias": alias,
            }
            port = topo.guess_port(ipv4net_info)

            if port:
                ip_entry["port"] = port
            if "mac" in ipv4net_info.keys():
                ip_entry["mac"] = ipv4net_info["mac"]
            if "circuit" in ipv4net_info.keys():
                ip_entry["circuit"] = ipv4net_info["circuit"].keys()
            if "oscars" in ipv4net_info.keys():
                ip_entry["oscars"] = ipv4net_info["oscars"].keys()
            if "bgp_peers" in address_info.keys():
                ip_entry["bgp_peers"] = []
                for bgp_peer_addr in address_info["bgp_peers"].keys():
                    bgp_info = address_info["bgp_peers"][bgp_peer_addr]
                    bgp_entry = {
                        "peer_addr": bgp_peer_addr,
                        "remote_as": bgp_info["remote_as"]
                    }
                    ip_entry["bgp_peers"].append(bgp_entry)

            ip_addrs.append(ip_entry)

    return ip_addrs


if __name__ == '__main__':
    main()
