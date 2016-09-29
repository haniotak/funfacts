#!/usr/bin/env python
# encoding: utf-8

import json
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/isis.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    ipv4nets = data["today"]["ipv4net"]
    latency_db = data["latency"]
    isis = get_isis_neighbors(ipv4nets=ipv4nets, latency_db=latency_db)
    graph = make_isis_graph(isis=isis)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(graph, outfile, indent=2)


def latency_of(addr=None, latency_db=None):
    if addr in latency_db:
        return latency_db[addr]["latency"]
    return None


def make_isis_graph(isis=None):
    edges = []
    for addr in isis.keys():
        entry = isis[addr]
        neighbor = entry["isis_neighbor"]
        if neighbor in isis:
            neighbor_entry = isis[neighbor]
            check_isis_neighborship(entry, neighbor_entry)
            edge = {
                "a": entry["router"],
                "z": neighbor_entry["router"],
                "mbps": entry["mbps"],
                "a_ifce": entry["int_name"],
                "z_ifce": neighbor_entry["int_name"],
                "a_addr": addr,
                "z_addr": neighbor,
                "isis_cost": entry["isis_cost"],
                "admin": entry["admin"],
                "latency": entry["latency"]
            }
            if "port" in entry.keys():
                edge["a_port"] = entry["port"]

            if "port" in neighbor_entry.keys():
                edge["z_port"] = neighbor_entry["port"]

            edges.append(edge)
    return edges


def check_isis_neighborship(entry_a, entry_b):
    assert entry_a["isis_neighbor"] == entry_b["address"], "%s %s " % (entry_a["isis_neighbor"], entry_b["address"])
    assert entry_b["isis_neighbor"] == entry_a["address"], "%s %s " % (entry_b["isis_neighbor"], entry_a["address"])


def get_isis_neighbors(ipv4nets=None, latency_db=None):
    isis = {}

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]
            ip_addr = ipv4net_info["ip_addr"]
            admin = ipv4net_info["admin"]

            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]

            if len(ip_addr.keys()) == 1:
                address = ip_addr.keys()[0]
                address_info = ip_addr[address]
                mask = address_info["mask"]

                if "isis_cost" in address_info.keys():
                    isis_cost = address_info["isis_cost"]
                    isis_status = address_info["isis_status"]

                    if isis_status:
                        isis_neighbor = address_info["isis_neighbor"]
                        latency = latency_of(addr=address, latency_db=latency_db)
                        if latency:

                            isis_entry = {
                                "address": address,
                                "router": router,
                                "latency": latency,
                                "mask": mask,
                                "int_name": int_name,
                                "admin": admin,
                                "mbps": mbps,
                                "isis_neighbor": isis_neighbor,
                                "isis_cost": isis_cost
                            }
                            port = topo.guess_port(ipv4net_info)

                            if port:
                                isis_entry["port"] = port

                            isis[address] = isis_entry

    return isis


if __name__ == '__main__':
    main()
