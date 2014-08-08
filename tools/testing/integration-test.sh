#   This script performs continuous integration.

# Step 1 --- Compilations succeeds
sbt/sbt compile
if [ $? -ne 0 ]; then
   echo "Compilation failed"
   exit 1
fi

# Step 2 --- Run JUnit Tests
sbt/sbt test 2> /dev/null 1> tools/testing/output/tmp
if [ $? -ne 0 ]; then
   cat 'tools/testing/output/tmp'
   echo "Unit tests have failed"
   exit 1
fi

# Step 3 --- All examples pass
numTrials=100
tools/testing/run-examples.sh $numTrials > /dev/null
errorLines=$(cat tools/testing/output/errorExamples.txt | wc -l);

if [ "$errorLines" != "1" ]; then
    echo "Not all BLOG Examples compile"
    cat 'tools/testing/output/errorExamples.txt'
    exit 1
fi	
