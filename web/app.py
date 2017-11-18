#
#
# Author: Cheng-Lai Huang
# Author: Dan Wang
# Author: Lei Li (leili@cs.berkeley.edu)
# Since: 2012-02
# Last modified: 2013-12-25

import web
import time
import os
import subprocess
import re
from operator import itemgetter
BLOG_EXTENSION = ".blog"
USER_STORE = "static/user_query/"
DEFAULT_GRAPH = "static/images/BerkeleyLogo.png"
EXAMPLE_BLOG_PATH = 'example.blog'
EXAMPLES_PATH = 'static/example/'
urls = ('/', 'blog_web_ui')
render = web.template.render('templates/')

app = web.application(urls, globals())

with open(EXAMPLE_BLOG_PATH) as f:
    example_blog_code = f.read()

def run_process(script_name,base,eng,alg):
    command = ["../blog", script_name,base,eng,alg]#"--displaycbn"
    #print command
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    output = os.linesep.join(p.communicate())
    return output

def ensure_dir(d):
    """ make sure the corresponding directory exists"""
    if not os.path.exists(d):
        os.makedirs(d)

def store_script(prefix, script):
    #Check if directory exist
    ensure_dir(USER_STORE)

    #Write input into local file
    script_name = prefix+BLOG_EXTENSION
    input_handler = open(script_name, 'w')
    input_handler.write(script)
    input_handler.close()
    return script_name

def execute_script(script,base,eng,alg):
    #Define name of the script
    current = str(time.time())
    filename = "tmp_%s" % current

    prefix = USER_STORE + filename

    script_name = store_script(prefix, script)
    return run_process(script_name,base,eng,alg)

class blog_web_ui:
    def GET(self):
        return render.index(example_blog_code)

    def POST(self):
        if hasattr(web.input(),'sel_file'):
            newpath = EXAMPLES_PATH+web.input().sel_file
            #print newpath
            with open(newpath) as f:
                example_blog_code = f.read()
            return example_blog_code
        s = web.input().textfield
        base = '-n '+web.input().base
        eng = '-e blog.engine.'+web.input().eng
        alg = '-s blog.sample.'+web.input().alg
        raw_data = execute_script(s,base,eng,alg)
        parsed_results = parse_query_results(raw_data)
        if not parsed_results:
            print "error occurred"
            return "error occurred"
        #return render.data(raw_data, parsed_results)
        return [parsed_results,raw_data]
def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        pass
    return False

def kdeit(dist):
    import numpy as np
    from pyqt_fit import kde
    #print dist
    samples,weights = zip(*dist)
    samples = list(samples)
    weights = list(weights)
    est = kde.KDE1D(samples,weights = weights)
    xmin = min(samples)
    xmax = max(samples)
    x = np.linspace(xmin,xmax, 200)
    y = est(x)
    return [list(i) for i in zip(x,y)]

def parse_query_results(s):
    results = []
    #print s
    regex = re.compile(r'Query Results\s*=*\n(([^=].*\n)*)\s*=*')
    for match in regex.finditer(s):
        #print match.groups()
        lines = match.groups()[0].split('\n')
        match = re.match('Number of samples: (\d+)', lines[0])
        if match:
            samples = match.group(1)
            results.append({
                'samples': samples,
                'queries': []
            })
            #print results
        dist = []
        checkden = False
        for line in lines[1:]:
            query_match = re.match('Distribution of values for (.*)', line)
            result_match = re.match(r'\s*(\S*)\s+([0-9]*\.?[0-9]+[eE]?[-+]?[0-9]*)', line)
            if query_match:
                query = query_match.group(1)
                if(len(dist)>0):
                    dist=sorted(dist, key=itemgetter(0))
                    if checkden:
                        checkden = False
                        dist = kdeit(dist)
                    results[-1]['queries'][-1]['distribution'].extend(dist)
                    dist=[]
                results[-1]['queries'].append({
                    'query': 'Distribution of values for '+query,
                    'distribution': [['value','probability']]
                })
                checkden = False
            elif result_match:
                value, probability = result_match.groups()
                if is_number(value):
                    if(value.count('.')==1):
                        checkden = True
                    dist.append([
                        float(value),
                        100 * float(probability)
                    ])
                else:
                    dist.append([
                        value,
                        100 * float(probability)
                    ])
        if(len(dist)>0):
            dist=sorted(dist, key=itemgetter(0))
            if checkden:
                checkden = False
                dist = kdeit(dist)
            results[-1]['queries'][-1]['distribution'].extend(dist)
            dist=[]
    #print results
    return results


if __name__ == '__main__':
    app.run()
