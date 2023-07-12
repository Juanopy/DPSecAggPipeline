#!/usr/bin/env python3

import numpy as np
from tofile import format_str

#PATH = os.environ['PRIVEE_PATH'] + "/listener/data"
PATH = "./listener/data"

def gen_history_file(nr_entries: int, prob, dev):
    write_to_file(
        [gen_fake(prob, dev) for _ in range(nr_entries)]
    )
        
def write_to_file(data):
    with open(PATH + "/hist.dat", "w") as f:
        f.write("".join(map(format_str, data)))
        f.close()

def gen_fake(prob, dev, label=None):
    return [
        "fca132110e2f1bff",
        gen_label() if label is None else label,
        gen_prob(prob),
        gen_nr_devices(dev, 20),
    ]

LABELS = [
    "airport",
    "bus",
    "metro",
    "metro_station",
    "park",
    "public_square",
    "shopping_mall",
    "street_pedestrian",
    "street_traffic",
    "tram"
]

def gen_label():
    return LABELS[np.random.randint(len(LABELS))]
    
def gen_label():
    return LABELS[np.random.randint(len(LABELS))]

def laplace(near: int, scale: float):
    return max(0, int(np.round(np.random.laplace(near, scale))))

def gen_prob(near: int):
    return max(15, laplace(near, near/5))

def gen_nr_devices(near, width):
    k = width//2
    return int(max(near-k, min(near+k, laplace(near, near/16))))

def usage():
    print("Use this script to generate some synthetic istory")
    print()
    print(f"{argv[0]} #ENTRIES #DEVICES")
    print("Params:")
    print("\t #ENTRIES - The number of entries to generate (over all categories)")
    print("\t #DEVICES - The number of devices mostly detected")
    print()
    print("The server has to get restartet for updating history")
    exit(1)

if __name__ == '__main__':
    from sys import argv

    if len(argv) < 3:
        usage()
    else:
        gen_history_file(int(argv[1]), np.random.randint(15, 50), int(argv[2]))