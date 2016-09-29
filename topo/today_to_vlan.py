#!/usr/bin/env python
# encoding: utf-8

import json

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/vlan.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    today_vlans = data["today"]["VLAN"]
    vlans = get_vlans(today_vlans=today_vlans)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(vlans, outfile, indent=2)


def get_vlans(today_vlans=None):
    vlans = []

    for vlan_id in today_vlans.keys():
        by_vlan_id = today_vlans[vlan_id]
        for router in by_vlan_id.keys():
            for snmp_idx in by_vlan_id[router].keys():
                vlan_info = by_vlan_id[router][snmp_idx]

                mbps = vlan_info["high_speed"]
                int_name = vlan_info["int_name"]
                alias = vlan_info["alias"]
                admin = vlan_info["admin"]
                entry = {
                    "vlan_id": int(vlan_id),
                    "router": router,
                    "int_name": int_name,
                    "alias": alias,
                    "admin": admin,
                    "mbps": mbps
                }
                if "ip_addr" in vlan_info.keys():
                    address = vlan_info["ip_addr"].keys()[0]
                    entry["address"] = address

                vlans.append(entry)

    return vlans


if __name__ == '__main__':
    main()
