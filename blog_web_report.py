""" Run the specified blog file and generate a html file reporting
    the convergence graph and the generated CBN.

    @author rbharath
    @date September 16th, 2012
"""

import os
import sys
import cgi
import subprocess

usage = "Accepts exactly one argument, a blog file!"
model = "example/figures/model"
error = "example/figures/error"
out = ""

if len(sys.argv) != 2:
    print usage
    print sys.argv
    sys.exit(1)
blog = sys.argv[1]
parts = blog.split(".")
name = os.path.basename(parts[0])
if len(parts) != 2 or (parts[1] != "blog" and parts[1] != "dblog"):
    print usage
    print parts
    sys.exit(1)

fname = name + ".html"
f = open(fname, "w")
command = ["python", "run_examples.py", "-e", blog]
subprocess.call(command)

for m in os.listdir(model):
    if name in str(m):
        model_name = m
        break
for m in os.listdir(error):
    if name in str(m):
        error_name = m
        break

model_path = os.path.join(model, model_name)
print "model_path: " + model_path
error_path = os.path.join(error, error_name)
print "error_path: " + error_path

out += "<html>\n"
out += "<body>\n"
out += "<h1>\n"
out += blog + "\n"
out += "</h1>\n"
out += "<p>\n"
out += "<img src=\"" + cgi.escape(model_path) + "\"/>\n"
out += "</p>\n"
out += "<p>\n"
out += "<img src=\"" + cgi.escape(error_path) + "\"/>\n"
out += "</p>\n"
out += "</body>\n"
out += "</html>\n"
f.write(out)
f.close()
if os.name == 'mac':
    command = ["open", fname]
    subprocess.call(command)
    pass
elif os.name == 'posix':
    command = ["xdg-open", fname]
    subprocess.call(command)
