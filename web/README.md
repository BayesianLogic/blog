Setting up Local Server 
=====================
1. `$ sudo apt-get install python-webpy`
2. `$ git clone https://github.com/amatsukawa/dblog.git`
3. `$ cd dblog`
4. `$ ./compile.sh`
5. `$ cd web`
6. `$ python app.py <port>`

Setting Remote Server
=====================
1. `$ start-server.sh`

Setting Remote Server to start at system boot time
=====================
1. add the following line to /etc/rc.local
  `cd /home/local/dblog-server/dblog/web/ && ./start-server.sh`

