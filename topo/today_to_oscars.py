#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo

INPUT_FILENAME = "input/today.json"
OUTPUT_FILENAME = "output/oscars.json"


def main():
    in_str = open(INPUT_FILENAME).read()
    data = json.loads(in_str)

    oscars = data["today"]["oscars"]
    vcs = get_vcs(oscars=oscars)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(vcs, outfile, indent=2)


def get_vcs(oscars=None):
    pp = pprint.PrettyPrinter(indent=4)
    vcs = []

    for gri in oscars.keys():
        vc_info = oscars[gri]
        exploded_path = []
        for hop in vc_info["path"]:
            parts = hop.split(":")
            path_entry = {
                "router": parts[0],
                "int_name": parts[1],
            }
            exploded_path.append(path_entry)

        entry = {
            "mbps": vc_info["bandwidth"] / 1000000,
            "path": exploded_path,
            "gri": gri,
            "description": vc_info["description"]
        }
        vcs.append(entry)

    return vcs


if __name__ == '__main__':
    main()
