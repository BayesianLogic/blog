import matplotlib.pyplot as plt
import numpy as np

from laser import plot_lasers


if __name__ == "__main__":
    # Evidence:
    lasers = [
        1.4199551039020832, 1.4731361624855104, 1.4436131301697084,
        1.4572615502637063, 1.3818078606039672, 1.3687651038986963,
        1.3202168874570337, 1.3715753314539574, 1.3721758763459488,
        1.399093844956726, 1.358123904515823, 1.350287944890654,
        1.2902129359809196, 1.339439458913098, 1.3431830497800328,
        1.2663116131209702, 1.321515095229276, 1.301266152244014,
        1.2949319600032894, 1.3700142577608263, 1.3023812346628272,
        1.2591859372908774, 1.2709198680252611, 1.2415879117347057,
        1.2637417586446253, 1.1772705800321128, 1.241614097023149,
        1.3414292522039288, 1.2139807494269221, 1.1986237212048532,
        1.24155580954343, 1.2375759843680423, 1.2337814199416541,
        1.3295921610521209, 1.2294210822225558, 1.2629927204270823,
        1.2234907456024364, 1.2780287360114053, 1.2428012066743475,
        1.190791801527358, 1.2633346045432017, 1.260993501518708,
        1.2238841404422418, 1.2699682004204973, 1.1936357824323205,
        1.2410265011533268, 1.2056770654352267, 1.2052938148732344,
        1.2434556173502795, 1.2667187594638853, 1.2966328007349615,
        1.2589816489307937, 1.2508116003922105, 1.2634025079913003,
        1.275572517345579, 1.2601771264762125, 1.3007842542550625,
        1.2191865115587874, 1.3301145130402796, 1.253305186444807,
        1.222302401732038, 1.3093885323917869, 1.3457086105170502,
        1.3422420257776462, 1.2661769112465617, 1.2785705778110903,
        1.3006652570747521, 1.372922576727234, 1.3574273697952886,
        1.3157702945734109, 1.3235364101763945, 1.3442175739841726,
        1.3773032523908955, 1.427231130670657, 1.4252110950809806,
        1.4157430190818714, 1.3854659089442256, 1.4529858907696929,
        1.4732879862443495, 1.4640517987822346, 1.4613769019510843,
        1.4349704122824665, 1.567304188572423, 1.509460005702215,
        1.5824457404757255, 1.6250242604965395, 1.6197487817741285,
        1.6353352169056132, 1.660471539198994, 1.7560063250241544,
        1.8077205353672006, 1.838540403172595, 10.054093030676617,
        10.014434639265435, 9.968598207426432, 9.985568091634832,
        10.024436713461354, 10.009734161697713, 9.980665257697126,
        9.981122147386538, 9.997464492087312, 10.00459938020436,
        9.974584206280293, 10.073223846159872, 9.963493197375072,
        9.923831561409697, 9.961676096097554, 9.953301790679241,
        9.988283469520724, 10.001654430918201, 9.997128852910024,
        9.984470524175762, 9.978102392300023, 10.049291050431474,
        9.961691611516338, 9.973211678264743, 9.961256182169954,
        10.042863179743112, 9.995934754303711, 9.9882978990478,
        4.9096573052986185, 4.726176666637595, 4.547470643664788,
        4.463441860856923, 4.454538584401141, 4.421377541756715,
        4.388004008906613, 4.307120669944673, 4.258903352061572,
        4.30486575399156, 4.225511305381533, 4.234459854395014,
        4.1792036386377935, 4.156006509540297, 4.172185764558416,
        4.176063702225215, 4.135108836624958, 4.081600178814032,
        4.089178345746217, 4.093797732176954, 4.102659983046924,
        4.135130657261892, 4.121391050566131, 4.0944104513484065,
        4.128231212345857, 4.123337283059184, 4.172137435174618,
        4.091489697596661, 4.1347548669915, 4.194455096578763,
        4.128358568590809, 4.20522689487838, 4.163991970995711,
        4.188481120370827, 4.294074272377757, 4.213240059795057,
        4.330682453332342, 4.296763867909687, 4.326507449784996,
        4.378972764746034, 4.325107497021552, 4.5206956662506865,
        4.463310686644484, 4.589736717634612, 4.677252978508529,
        4.900346549540458, 9.999150016758994, 10.00178392264997,
        10.002622891468729, 10.01290568847242, 9.97792325569343,
        9.966695853010453, 10.021734985901348, 10.001183926895399,
        10.008700299747876, 10.039206020579392, 10.040655848760446,
        9.986095997432857, 10.016110327339693, 9.967346353147908,
        9.963459389917166, 9.964099862561012, 10.003927319861392,
        9.982202079471872, 9.993574772509787, 9.982131736288416,
        9.950117334011484, 9.959210086850254, 9.96725459529896,
        10.064011272887424, 10.004522347370013, 9.97429388344621,
        9.993502529120795, 10.016276065257259, 10.061158453705469,
        10.02135661550733, 10.01544021717918, 10.013385206592027,
        9.952457487150966, 10.009492923865977, 10.030405317938948,
        9.962056398686663, 9.98891284938373, 9.97928281241928,
        9.991675949439731, 9.973859740988631, 10.018464641644856,
        9.992485053602335, 9.982862160275738, 10.028830361937327,
        9.939014426719325, 9.999028190984957, 9.975253012956284,
        9.992630280834454, 10.03150641206209, 10.0959924403478,
        10.009008603809, 10.033385149492334, 9.966849704659351,
        10.024500080428268, 10.003654187153451, 10.022231177136943,
        9.976048953976747, 9.984159793982375, 10.007382305828084,
        9.982991936079028, 10.008609002157892, 10.01542575021886,
        10.01451735762041, 9.978268942086638, 9.986596777088824,
        9.99192394296068, 9.987097722790663, 9.976216557091758,
        9.99635245010135, 10.022099203438176, 9.950815688173602,
        10.052315150420217, 9.999020969520053, 10.000180337153763,
        10.030962015172253, 9.985332924786977, 10.002328843920248,
        10.02376851767376, 9.995250894002162, 9.943366700815025,
        9.958204046011476, 9.973001088865276, 10.002705692264856,
        10.024474229570538, 9.953341479399018, 9.976909372046261,
        10.017086057111253, 9.989535642053792, 10.005096455800272,
        9.99212670773365, 10.00013436103505, 9.965281517641214,
        9.98796999640154, 9.982508493419578, 9.99556216104579,
        9.968697665049374, 9.967378661666505, 9.996883512422901,
        10.052216024499264, 10.058965133451744, 9.987745234698338,
        9.996041463982712, 10.024307315644363, 9.952503317420145,
        9.984501738692842, 9.987864935066025, 9.991371418574566,
        10.036517643720636, 10.078775910523378, 9.994934387929087,
        10.028249771874593, 9.988192380196345, 9.977887439188693,
        9.97434815833694, 10.030465117614154, 10.00336385125315,
        10.031642791936317, 10.021736466560347, 10.01424179573825,
        9.973652188285655, 9.979664284144501, 9.976491423537873,
        9.960508183456517, 10.040840850954044, 10.023784468903472,
        10.01709311985391, 9.945349910360774, 10.018816559176184,
        10.040530649992448, 10.021424914929089, 10.004121179059767,
        9.973284840769757, 10.012051655547745, 9.952577368054069,
        10.041954046689265, 9.965966083565728, 9.954168749100917,
        9.953891040275725, 10.040394857902019, 10.106960843148665,
        10.02419217674276, 10.007271885918604, 10.027690463586508,
        10.001136946730501, 10.037974132075735, 9.98482258218477,
        10.026056883265621, 9.993750244410773, 9.990218243936738,
        9.9558928378501, 9.981917600061367, 10.00726895562217,
        9.958745330150578, 10.006409585528777, 9.989566590726191,
        10.053623441839168, 10.013875645067953, 10.065853308780797,
        9.93051435764753, 9.996635566268784, 10.049913314951485,
        10.025412607468255, 9.986779057119039, 9.999647490660815,
        10.004817074863535, 10.043355591753151, 9.997462415615892,
        9.972829949216653, 10.039108563888444, 9.993558837439922,
        9.985286209488928, 10.04012460410906, 10.043322672135142,
        10.046141736859282, 10.030860347503477, 10.017644325283536,
        10.098833736862332, 9.980101285575163, 9.941566867327724,
        9.965226912196165, 9.986925087670146, 9.95521308131201,
        9.964316650728485, 10.013582876147453, 9.98189769852767,
        9.915675691172165, 9.988639135792132, 10.017539290128836,
        10.065554804383995, 10.027229939557479, 10.03950095363247,
        10.014373411286332, 10.012849426860443, 9.955870855059626,
        10.0023486034769
    ]

    # True vehicle location:
    x = 6.0
    y = 4.0
    theta = 1.7

    # LW, 100K samples, sigma = 0.001 * eye or 0.01 * eye
    # results in all samples having zero likelihood.

    # LW, 100K samples, sigma = 0.1 * eye, trial 1:
    x = 5.933714824453618
    y = 3.645168075995226
    theta = 1.7602662352791922

    # LW, 100K samples, sigma = 0.1 * eye, trial 2:
    x = 6.288088918010219
    y = 4.373940752990642
    theta = 1.7315791301475265

    # MH, 100K samples, sigma = 0.1 * eye, trial 1, weighted average:
    x = 5.9914188919234261
    y = 6.9911134074268464
    theta = 2.3398027558646413

    # MH, 100K samples, sigma = 0.1 * eye, trial 2, MAP:
    x = 4.450117201676392
    y = 4.882172882515441
    theta = 1.32124882567211

    # MH, 100K samples, sigma = 0.1 * eye, trial 3, MAP:
    x = 5.8567944670210075
    y = 7.108483334317908
    theta = 2.265766763721638

    laser_angles = np.arange(-90, 90.5, 0.5) * np.pi / 180
    laser_max_range = 10
    obstacles = [
        (7.0, 9.0, 1.0),
        (8.0, 5.0, 1.0)
    ]
    plot_lasers(
        x, y, theta, laser_angles, laser_max_range,
        obstacles, lasers, plt.gca())
    plt.show()