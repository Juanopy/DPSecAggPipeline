# https://gist.github.com/mdonkers/63e115cc0c79b4f6b8b3a6b797e485c7
# !/usr/bin/env python3
"""
Very simple HTTP server in python for logging requests
Usage::
    ./server.py [<port>]
"""
import base64
from http.server import BaseHTTPRequestHandler, HTTPServer
import logging
import datetime

from tofile import tofile


class S(BaseHTTPRequestHandler):
    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
        self._set_response()
        self.wfile.write("GET request for {}".format(self.path).encode('utf-8'))

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])  # <--- Gets the size of data
        post_data = self.rfile.read(content_length)  # <--- Gets the data itself
        # logging.info("POST request,\nPath: %s\nHeaders:\n%s\n\nBody:\n%s\n",str(self.path), str(self.headers), post_data.decode('utf-8'))

        self._set_response()
        self.wfile.write("POST request for {}".format(self.path).encode('utf-8'))
        datastring = str(post_data.decode('utf-8'))

        # TODO id in datastring, or sent in header
        dev_id = 'something'
        tofile(dev_id, datastring)
        print(datastring)
        # datastring = datastring.split("=")[1]
        # print(datastring)
        # datastring = base64.b64decode(datastring).decode('utf-8')

        # #write to file and clear file before writing
        # with open("data.txt", "a") as f:
        #     f.write("\n")
        #     f.write(str(datetime.datetime.now()))
        #     f.write("\n")
        #     f.write(datastring)
        #     f.write("\n")
        #     f.close()


def run(server_class=HTTPServer, handler_class=S, port=41071):
    logging.basicConfig(level=logging.INFO)
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    logging.info('Starting httpd...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    logging.info('Stopping httpd...\n')


if __name__ == '__main__':
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()