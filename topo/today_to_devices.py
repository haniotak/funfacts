#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/devices.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    routers = data["today"]["router_system"]
    devices = get_devices(routers=routers)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(devices, outfile, indent=2)


def get_devices(routers=None):
    pp = pprint.PrettyPrinter(indent=4)
    devices = []

    for router_name in routers.keys():
        router_info = routers[router_name]
        entry = {
            "name": router_name,
            "os": router_info["os"],
            "description": router_info["description"]
        }
        devices.append(entry)

    return devices


if __name__ == '__main__':
    main()
