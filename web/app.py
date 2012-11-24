from run_examples import BlogParser
import pygraphviz as pgv
import pygraph
import web
import time
import os
import subprocess
import json

BLOG_EXTENSION = ".blog"
USER_STORE = "static/user_query/"
DEFAULT_GRAPH = "static/images/BerkeleyLogo.png"

urls = ('/', 'blog_web_ui')
render = web.template.render('templates/')

app = web.application(urls, globals())

my_form = web.form.Form(
                web.form.Textbox('', class_='code', id='code', cols="25", rows="23"),
                )

def generate_graph(prefix, output):
    IMAGE_EXTENSION = ".png"
    blog_parser = BlogParser()
    graph, data = blog_parser.parse_blog_output(output)
    if graph is not None:
        dot = pygraph.readwrite.dot.write(graph)
        G = pgv.AGraph(dot)
        G.layout(prog="dot")
        cbn_name = prefix + IMAGE_EXTENSION
        G.draw(cbn_name)
        
        return cbn_name
    return DEFAULT_GRAPH

def run_process(script_name):
    command = ["./run.sh", "--print", script_name]
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    output = p.communicate()[0]
    returncode = p.returncode
    return output, returncode
 
def store_script(prefix, script):
    #Write input into local file
    script_name = prefix+BLOG_EXTENSION
    input_handler = open(script_name, 'w')
    input_handler.write(script)
    input_handler.close()
    return script_name
        
def execute_script(script):
    #Define name of the script 
    current = str(time.time())
    filename = "tmp_%s" % current
    
    prefix = USER_STORE + filename
    
    script_name = store_script(prefix, script)
    output, returncode = run_process(script_name)
    
    graph = DEFAULT_GRAPH
    if returncode == 0:
        graph = generate_graph(prefix, output)
    
    #Run commandline Blog on the file and return output
    return json.dumps({'text_result': text_to_html(output), "graph_result": graph})

# A ad hoc text to html converter
# Should use other library if output include special symbol
def text_to_html(text):
    return text.replace("\n", "<br/>")


class blog_web_ui:
    def GET(self):
        form = my_form()
        return render.index(form, "Your result will appear here.")
        
    def POST(self):     
        s = web.input().textfield 
        return execute_script(s)

if __name__ == '__main__':
    app.run()