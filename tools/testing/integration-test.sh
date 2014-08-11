#   This script performs continuous integration.
TMP=tools/testing/output/tmp
mkdir -p tools/testing/output

# Step 1 --- Compilations succeeds
sbt/sbt compile 1> $TMP 2>> $TMP 
if [ $? -ne 0 ]; then
   echo "Compilation failed"
   exit 1
fi

# Step 2 --- Run JUnit Tests
sbt/sbt test 1> $TMP 2>> $TMP
if [ $? -ne 0 ]; then
   echo "Unit tests have failed"
   cat $TMP
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
