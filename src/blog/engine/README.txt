Evaluating BSQ policies:
The main method is in blog/engine/experiments/DepthFirstPolicyEvaluationRunner.java
The parameters are
model_file -q query_file -p policy_file -n num_particles -t num_steps -s log_file_prefix -f output_directory

For example
~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_discounted.mblog -n 1000 -t 5 -p ~/workspace/dblog/ex_inprog/monster/monster_4doors_bsq_policy -q ~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_query -s target.out -f temp3



Running OUPBVI:
The main method is in blog/engine/pbvi/OUPBVI.java.
The parameters are 
model_file query_file num_particles num_steps num_beliefs detailed_timing_prints? use_perseus? 

For example
~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_soundsymbols.mblog  ~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_query 200 2 8 false false
