#!/bin/bash -eu

# To save results:
#   ./eval_all.sh 2>&1 |tee all.out

datasets="1_straight 2_bend 3_curvy 4_circle 5_eight 6_loop 7_random"
datasets_array=( $datasets )
for dataset in ${datasets_array[@]}; do
    echo
    echo "${dataset}:"

    # Generate car.blog in the current directory:
    python -m ppaml_car.blog_gen $dataset noisy

    # Run BLOG particle filter and write out.json:
    time blog -e blog.engine.ParticleFilter -n 100 -r -k ppaml_car -w out.json car.blog >/dev/null

    # Evaluate results:
    python -m ppaml_car.evaluate $dataset out.json

done
