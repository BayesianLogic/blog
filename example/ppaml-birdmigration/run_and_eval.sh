mkdir cps
mkdir eval_results

cd ./cps
mkdir dataset1
mkdir dataset2
mkdir dataset3

cd ..

cd ./Birds1_On4by4Grid/
./run_bird.sh
python convert_output.py out.json estimation

cd ..

cd ./Birds1000_On10by10Grid/
./run_bird.sh
python convert_output.py out.json estimation
python convert_output.py out.json reconstruction
python convert_output.py out.json prediction

cd ..

cd ./Birds1M_On10by10Grid/
./run_bird.sh
python convert_output.py out.json estimation
python convert_output.py out.json reconstruction
python convert_output.py out.json prediction

cd ..
python birdcast_eval.py cps ground eval_results

cd ..

