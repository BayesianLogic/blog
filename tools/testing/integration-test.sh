#   This script performs continuous integration.

# Step 1 --- Compilations succeeds
sbt/sbt compile
if [ $? -ne 0 ]; then
   echo "Compilation failed"
   exit 1
fi

# Step 2 --- Run JUnit Tests
sbt/sbt test 2> /dev/null 1> /dev/null
if [ $? -ne 0]; then
   echo "Unit tests have failed"
   exit 1
fi

# Step 3 --- All examples pass
tools/testing/run-examples.sh > /dev/null
errorLines=$(cat tools/testing/output/errorExamples.txt | wc -l);

if [ "$errorLines" != "1" ]; then
    echo "Not all BLOG Examples compile"
    cat 'tools/testing/output/errorExamples.txt'
    exit 1
fi	

# Step 4 -- Verify that blog errors are caught
#errorLines=$(cat tools/testing/output_incorrect/incorrect_examples.txt)
#
#if [ "$errorLines" != "1"]; then
#    echo "Not all incorrect BLOG examples return a nonzero exit code"
#    exit 1
#fi
