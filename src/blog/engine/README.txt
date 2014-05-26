Evaluating BSQ policies:
The main method is in blog/engine/experiments/DepthFirstPolicyEvaluationRunner.java
The parameters are
model_file -q query_file -p policy_file -n num_particles -t num_steps -s log_file_prefix -f output_directory

For example
~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_discounted.mblog -n 1000 -t 5 -p ~/workspace/dblog/ex_inprog/monster/monster_4doors_bsq_policy -q ~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_query -s target.out -f temp3



Running OUPBVI:
The main method is in blog/engine/pbvi/OUPBVI.java.
The parameters are 
model_file -q query_file -n num_particles -t num_steps -b num_beliefs -d(for detailed timing prints) perseus 

For example
~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_soundsymbols.mblog -q ~/workspace/dblog/ex_inprog/monster/monster_standard_noisy_4doors_query -n 200 -t 2 -b 8 -d 
