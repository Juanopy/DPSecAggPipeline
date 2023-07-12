import base64
import datetime
import urllib.parse
import numpy as np
import logging
import os

#PATH = os.environ['PRIVEE_PATH'] + "/listener/data"
PATH = "./listener/data"

def tofile(dat: str):
    datastring = to_str(dat)
    logging.info(datastring)

    id = datastring.split(":")[1].split(",")[0]

    # data_tuple = add_noise(extract_data(datastring))
    data_tuple = extract_data(datastring)

    write_to_file(id, data_tuple)

def to_str(dat):
    datastring = dat.split("=")[1]
    #print("1" + datastring)
    datastring = urllib.parse.unquote(datastring)
    #print("2" +datastring)
    datastring = base64.urlsafe_b64decode(datastring).decode('utf-8')
    return datastring


def write_to_file(id, data):
    with open(f"{PATH}/{id}.dat", "a") as f:
        f.write(format_str(data))
        # f.write(format_str_old(datastring))
        f.close()


def format_str(data: list):
    t = str(datetime.datetime.now() + datetime.timedelta(hours=1))
    return','.join([t, *(str(d) for d in data)])+"\n"

def extract_data(s: str):
    return [
        d.split(':')[1] for d in s.split(',')
    ]

def add_noise(data: list, s=1, eps=1):
    out = data.copy()
    out[-1] = str(max(int(out[-1]) + int(np.round(np.random.laplace(0, s/eps))), 0))
    return out