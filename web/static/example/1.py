import os
from os import listdir
from os.path import isfile, join
mypath = "/mnt/c/Users/94696/Dropbox/linux_blog/blog/web/static/example/"

onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
for file in onlyfiles:

	if file[-1]=='g':
		if file == 'burglary.blog':
			print '<option selected>'+file +'</option>'
		else:
			print '<option>'+file +'</option>'