#!/bin/sh

#PBS -e localhost:/work/leili/work/dblog/output.err
#PBS -o localhost:/work/leili/work/dblog/output.out

##############################################################################
### MAKE SURE TO CHANGE THE NAME OF THE JOB SO YOU KNOW WHICH ONE IT IS... ###
##############################################################################

#PBS -N final_10M_samples 

#PBS -q psi
#PBS -l nodes=1:ppn=1
#PBS -l mem=4g

NUMLABELS='45'
#NUMLABELS='10'
#NUMLABELS='1'

NUMITERS='10000000'
BURNIN='2000000'

#NUMITERS='6000000'
#BURNIN='2000000'

#NUMITERS='1'
#BURNIN='0'

cd ~/work/dblog

# Experiment for asking who is a professor at NYU
#exec ~/work/dblog/blog -s expmt.RelationExtractionMHSampler -p expmt.RelationExtractionProposer -n $NUMITERS -q $NUMITERS -b $BURNIN ~/work/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/Rel_Extract_full.blog ~/work/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/question_experiment/full_test_"$NUMLABELS"_labels.eblog ~/work/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/question_experiment/full_test_NYU.qblog > /work/leili/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/question_experiment/full_results_probs_"$NUMLABELS"_labels_"$NUMITERS"_samples.txt

# Final Experiment
exec ~/work/dblog/blog -s expmt.RelationExtractionMHSampler -p expmt.RelationExtractionProposer -n $NUMITERS -q $NUMITERS -b $BURNIN ~/work/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/Rel_Extract_full.blog ~/work/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/question_experiment/full_test_"$NUMLABELS"_labels.eblog ~/work/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/question_experiment/full_test.qblog > /work/leili/dblog/ex_inprog/Relation_Extraction/NYT_subsetdata_test/question_experiment/36queries_results_probs_"$NUMLABELS"_labels_"$NUMITERS"_samples.txt
