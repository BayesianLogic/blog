**Folders**:  
1) Birds1_On4by4Grid: Corresponds to DataSet 1, recommended number of particles: , takes minutes on a 2011 MacBookPro with 2GHz Intel i7 processor and 4GB RAM.  
2) Birds1_On4by4Grid: Corresponds to DataSet 2, recommended number of particles: , takes minutes on a 2011 MacBookPro with 2GHz Intel i7 processor and 4GB RAM.  
3) Birds1_On4by4Grid: Corresponds to DataSet 3, recommended number of particles: , takes minutes on a 2011 MacBookPro with 2GHz Intel i7 processor and 4GB RAM.  
For inference we are using particle filtering which is a Sequential Monte Carlo algorithm.  
**How to Run**:  
```
time blog -e blog.engine.ParticleFilter LocToInteger.blog bird_flow_probs.blog bird_features.blog bird_model.blog bird_obs.blog bird_queries.blog -n 100 -q 1 -w out.json
```  
which will output the particles for queried variables at each timestep, total computation time and dump all the particle information into out.json  
*Prepared by* Yusuf Bugra Erol  
*Contact at* yusufbugraerol@berkeley.edu