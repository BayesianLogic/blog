#   This script performs continuous integration.

# Step 1 --- Compilations succeeds
sbt/sbt compile
if [ $? -ne 0 ]; then
   echo "Compilation failed"
   exit 1
fi

# Step 2 --- Run JUnit Tests


# Step 3 --- All examples pass
tools/run-examples.sh
errorLines=`cat tools/testing/errorExamples.txt | wc -l`;

if [ "$errorLines" != "1" ]; then
    cat 'tools/testing/errorExamples.txt'
fi	

# Step 4 --- Verify that blog errors are caught


# Step 5 --- A simple example (e.g. burglary) produces
# consistent output
