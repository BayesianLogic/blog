Setting up Local Server 
=====================
1. `$ sudo apt-get install python-webpy`
2. `$ git clone https://github.com/amatsukawa/dblog.git`
3. `$ cd dblog`
4. `$ ./compile.sh`
5. `$ cd web`
6. `$ python app.py <port>`

Setting Remote Server using screen (Temporary)
=====================
1. `$ Finish all the step on local, except step 5, 6`
2. `$ screen`
3. `$ cd <path_to_web_folder>`
4. `$ python app.py <port>`
5. `$ screen -d`
6. `$ logout `

