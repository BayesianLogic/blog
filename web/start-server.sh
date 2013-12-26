#!/bin/bash
# start the web server
if [ "$1" == "--test" ]; then
	python app.py 9000
else
  nohup nice python app.py 8080 > /var/log/dblog-server.log &
fi