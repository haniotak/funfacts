#!/usr/bin/env python
# encoding: utf-8

import json
import random

INPUT_PORTS = "output/ports.json"
INPUT_CIRCUITS = "output/circuits.json"
OUTPUT_FILENAME = "output/maintenance.json"

PORTS_MAINT = 10
DEVICES_MAINT = 3
CIRCUITS_MAINT = 5


def main():
    in_str = open(INPUT_PORTS).read()
    ports = json.loads(in_str)

    in_str = open(INPUT_CIRCUITS).read()
    circuits = json.loads(in_str)

    maintenance = make_maintenance(ports=ports, circuits=circuits)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(maintenance, outfile, indent=2)


def make_maintenance(ports=None, circuits=None):
    maintenance = []

    for i in range(0, PORTS_MAINT):
        routers_num = len(ports.keys())
        router_idx = random.randint(0, routers_num)
        router = ports.keys()[router_idx]

        ports_num = len(ports[router].keys())
        port_idx = random.randint(0, ports_num - 1)
        port = ports[router].keys()[port_idx]
        entry = {
            "type": "OUTAGE",
            "scope": "PORT",
            "router": router,
            "tickets": [make_ticket()],
            "port": port
        }

        maintenance.append(entry)

    for i in range(0, DEVICES_MAINT):
        routers_num = len(ports.keys())
        router_idx = random.randint(0, routers_num - 1)
        router = ports.keys()[router_idx]
        entry = {
            "type": "OUTAGE",
            "scope": "ROUTER",
            "tickets": [make_ticket()],
            "router": router
        }
        maintenance.append(entry)

    for i in range(0, CIRCUITS_MAINT):
        circuits_num = len(circuits.keys())
        circuit_idx = random.randint(0, circuits_num - 1)
        circuit = circuits.keys()[circuit_idx]
        entry = {
            "type": "OUTAGE",
            "scope": "CIRCUIT",
            "tickets": [make_ticket()],
            "circuit": circuit
        }
        maintenance.append(entry)
    return maintenance


def make_ticket():
    prefix = "ESNET-2016"
    month = random.randint(1, 12)
    if month < 10:
        month = "0%s" % month
    day = random.randint(1, 28)
    if day < 10:
        day = "0%s" % day

    ticket = random.randint(1, 120)
    if ticket < 10:
        ticket = "00%s" % ticket
    elif ticket < 100:
        ticket = "0%s" % ticket

    return "%s%s%s%s" % (prefix, month, day, ticket)


if __name__ == '__main__':
    main()
