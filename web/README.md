Setting up Local Server 
=====================
1. `$ sudo apt-get install python-webpy`
2. `$ git clone https://github.com/amatsukawa/dblog.git`
3. `$ cd dblog`
4. `$ ./compile.sh`
5. `$ cd web`
6. `$ ./start-server.sh`

Start the BLOG Web Server
=====================
1. `$ ./start-server.sh`

Testrun the BLOG Web Server
=====================
1. `$ ./start-server.sh --test`

Setting Remote Server to start at system boot time
=====================
1. add the following line to /etc/rc.local
  `cd /home/local/dblog-server/dblog/web/ && ./start-server.sh`
