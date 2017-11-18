Install Requirement
====================
BLOG web server requires python-webpy, install using
```
$ sudo apt-get install python-webpy
```
BLOG web server also requires python library pyqt-fit, install using
```
$ pip install pyqt_fit
```
If you meet this exception,
```
ImportError: cannot import name path
```
It is caused by a change in the path.py package. Reverting to an older version of path.py solves this :
```
pip install -I path.py==7.7.1
```

Start the BLOG Web Server
=====================
0. make sure the code is compiled. refer to document about how to compile. (under sbt, pls run `sbt/sbt stage`)
1. `$ ./start-server.sh`

Testrun the BLOG Web Server
=====================
1. `$ ./start-server.sh --test`

Setting Remote Server to start at system boot time
=====================
1. put blog directory under `/home/local/blog-server/`
2. add the following line to /etc/rc.local
  `cd /home/local/blog-server/blog/web/ && ./start-server.sh`
