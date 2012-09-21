from run_examples import *
import matplotlib.pyplot as plt
import matplotlib

SAMPLERS = ('blog.sample.modular.ModularLWSampler',
            'blog.sample.LWSampler')

# a bunch of globals imported from run_examples
output = ""
result = 0
process = None

from numpy import average, std
######
# This script is only meant to be used once to generate 1 plot for a demo,
# it's possibly the worst code/style for the long run.

# todo: rewrite for long term use
######

def run_once(example):
    rtn = {}
    for sampler in SAMPLERS:
        command = [blog, "--sampler", sampler, example,
                "-n", 50000, "-q", 5000, "--print", "-r"]
        run_with_timeout(" ".join([str(c) for c in command]), 100)
        data = BlogParser().parse_blog_output(output)[1]
        rtn[sampler] = data
    return rtn

def load_solution(example):
    solutions = "example/solutions"
    solution_path = os.path.join(solutions, os.path.basename(example))
    solution = open(solution_path)
    output = solution.read()
    solution_graph, solution_data = BlogParser().parse_blog_output(output)
    return solution_data

def run_with_timeout(command, timeout):
    """ Run given command for timeout seconds. If command does not exit
        before timeout is reached, forcibly kill spawned process.
    """
    global result
    global output
    global process
    def thread_fun():
        global process
        global output
        global result
        print "Blog Thread Running " + command + " started"
        process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE,
                stderr=subprocess.PIPE, preexec_fn=os.setsid)
        output = process.communicate()[0]
        result = process.returncode
    thread = threading.Thread(target=thread_fun)
    thread.start()
    thread.join(timeout)

    if thread.isAlive():
        print "Killing Example"
        try:
            output = ""
            result = -1
            #process.kill()
            os.killpg(process.pid, signal.SIGTERM)
        except OSError:
            pass
        print "Going To Sleep"
        time.sleep(10)
        print "Woken Up"
        thread.join()
    return

if __name__ == "__main__":
    """
    example = "example/simple-aircraft.blog"
    correct = load_solution(example)
    all_data = {}
    runs = 100
    for _ in xrange(runs):
        test = run_once(example)
        distances = {}
        for sampler, vals in test.items():
            a = {}
            for k,v in vals.items():
                true_distr = correct[k][0]
                for num_samples in v:
                    a[num_samples] = variation_distance(v[num_samples], true_distr)
            distances[sampler] = a
        for sampler, data in distances.items():
            if sampler not in all_data:
                all_data[sampler] = {}
            b = all_data[sampler]
            for num_samples, div_val in data.items():
                if num_samples not in b:
                    b[num_samples] = [div_val]
                else:
                    b[num_samples].append(div_val)

    plot_data = {}
    for sampler, data in all_data.items():
        avg_data = {}
        for num_samples, exp_vals in data.items():
            avg_data[num_samples] = (average(exp_vals), std(exp_vals))
        plot_data[sampler] = avg_data

    print plot_data
    """

    runs = 100
    plot_data = {'blog.sample.modular.ModularLWSampler': {20000: (0.0090088764049257603, 0.0059626518035629326), 40000: (0.0051295987540050884, 0.0037926238436156679), 5000: (0.016461270306315289, 0.011286800888789991), 30000: (0.00676463562883888, 0.0047874722840120032), 10000: (0.012686637047366152, 0.0091102732914102053), 25000: (0.0074945879503821476, 0.0055322528628447267), 45000: (0.0046266231166619039, 0.00353674503390084), 50000: (0.0046169262472509059, 0.0036280162079916404), 15000: (0.010086430627383592, 0.0071400395813094814), 35000: (0.0055187024030445338, 0.0040552151539162218)}, 'blog.sample.LWSampler': {20000: (0.032120820490631384, 0.019299084095594065), 40000: (0.023260968942431223, 0.012085969638578156), 5000: (0.062838679608073753, 0.039831180964683979), 30000: (0.027388149863334661, 0.014434247672735968), 10000: (0.046663907443073381, 0.028286815806336695), 25000: (0.029055260603563431, 0.016984439920387694), 45000: (0.021334363196284251, 0.012139074581665124), 50000: (0.019143013432751453, 0.011350097873357817), 15000: (0.037356950574867355, 0.022439843912749047), 35000: (0.025378971998435692, 0.013460089860937692)}}

    colors = {'blog.sample.modular.ModularLWSampler': 'g',
              'blog.sample.LWSampler': 'r'}
    matplotlib.rcParams.update({'font.size': 16})
    f = plt.figure()
    for sampler, data in plot_data.items():
        data = sorted(data.items())
        x, y, yerr = [], [], []
        for p in data:
            x.append(p[0])
            y.append(p[1][0])
            yerr.append(p[1][1])
        plt.errorbar(x, y, yerr=yerr, color=colors[sampler],
                fmt='o-', label=sampler.split(".")[-1])
    plt.legend()
    plt.title("simple-aircraft.blog: #{Aircraft a : true}  [%d runs]" % runs)
    plt.ylabel("Variation Distance")
    plt.xlabel("Num Samples")
    plt.show()
