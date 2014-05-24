To run and evaluate  
```
./run_and_eval.sh
```
which will output the evaluation results to the folder ```eval_results```. The ground-truth datasets folder ```ground``` should be placed to the same folder as ```run_and_eval.sh```. Same applies to the evaluation script ```birdcast_eval.py``` and the blog model folders ```Birds1_On4by4Grid```, ```Birds1000_On10by10Grid``` and ```Birds1M_On10by10Grid```.

**Folders**:  
1) Birds1_On4by4Grid: Corresponds to DataSet 1, recommended number of particles: , takes minutes on a 2011 MacBookPro with 2GHz Intel i7 processor and 4GB RAM.  
2) Birds1000_On10by10Grid: Corresponds to DataSet 2, recommended number of particles: , takes minutes on a 2011 MacBookPro with 2GHz Intel i7 processor and 4GB RAM.  
3) Birds1M_On10by10Grid: Corresponds to DataSet 3, recommended number of particles: , takes minutes on a 2011 MacBookPro with 2GHz Intel i7 processor and 4GB RAM.  
For inference we are using particle filtering or Liu-West filtering (a variant of PF) which is a Sequential Monte Carlo algorithm.  
**How to Run**:  
-n: specifies number of particles, default 50000  
-w: specifies which file to write into (.json)  
-q: specifies the intervals for which the particles are printed on the terminal during run-time  
-e: specifies the inference engine (either Particle Filter or Liu-West Filter)
```
time blog -e blog.engine.ParticleFilter LocToInteger.blog bird_flow_probs.blog bird_features.blog bird_model.blog bird_obs.blog bird_queries.blog -n 100 -q 1 -w out.json
```  
or  
```
time blog -e blog.engine.LiuWestFilter LocToInteger.blog bird_flow_probs.blog bird_features.blog bird_model.blog bird_obs.blog bird_queries.blog -n 100 -q 1 -w out.json
```  
which will output the particles for queried variables at each timestep, total computation time and dump all the particle information into out.json

**Evaluation**  
Since we are approaching the challenge in Bayesian way, tasks of **reconstruction** (state estimation), **prediction** and **parameter estimation** are done simultaneously. The observations in bird_obs.blog span only the first 3 years (for dataset 2 and 3) whereas the queries are for 6 years. The parameter estimate at the end of the third year is also queried.  

*Prepared by* Yusuf Bugra Erol  
*Contact at* yusufbugraerol@berkeley.edu