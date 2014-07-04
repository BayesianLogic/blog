Install Requirement
====================
BLOG web server requires python-webpy, install using 
```
$ sudo apt-get install python-webpy
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
