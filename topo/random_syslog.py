#!/usr/bin/env python
# encoding: utf-8

import json
import random
import datetime

INPUT_DEVICES = "output/devices.json"
OUTPUT_FILENAME = "output/syslog.json"

LINES_TO_MAKE = 1000


def main():
    in_str = open(INPUT_DEVICES).read()
    devices = json.loads(in_str)

    syslog = make_syslog(devices=devices)

    with open(OUTPUT_FILENAME, 'w') as outfile:
        json.dump(syslog, outfile, indent=2)


def make_syslog(devices=None):
    syslog = {}
    timestamp = datetime.datetime.now() - datetime.timedelta(days=1)

    for i in range(0, LINES_TO_MAKE):
        routers_num = len(devices)
        router_idx = random.randint(0, routers_num -1)
        router = str(devices[router_idx]["name"])

        if router not in syslog.keys():
            syslog[router] = []
        deltasec = random.randint(10, 1000)
        timestamp = timestamp + datetime.timedelta(seconds=deltasec)

        line = "%s %s some text" % (timestamp.isoformat(), router.upper())
        syslog[router].append(line)

    return syslog


if __name__ == '__main__':
    main()
