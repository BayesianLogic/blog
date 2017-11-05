#   This script performs continuous integration.
TMP=tools/testing/output/tmp
mkdir -p tools/testing/output
echo "Starting test..."

# Step 1 --- Compilations succeeds
sbt/sbt compile 1> $TMP 2>> $TMP 
if [ $? -ne 0 ]; then
   echo "Compilation failed"
   exit 1
fi
echo "Compilation successful"

# Step 2 --- Run JUnit Tests
sbt/sbt test 1> $TMP 2>> $TMP
if [ $? -ne 0 ]; then
   echo "Unit tests have failed"
   cat $TMP
   exit 1
fi
echo "JUnit Tests pass"

# Step 3 --- All examples pass
numTrials=100
tools/testing/run-examples.sh $numTrials > /dev/null
errorLines=$(cat tools/testing/output/errorExamples.txt | wc -l);

if [ "$errorLines" != "1" ]; then
    echo "Not all BLOG Examples compile"
    cat 'tools/testing/output/errors.txt'
    exit 1
fi	
echo "BLOG Examples compile."

# Step 4 --- Run all the incorrect examples
tools/testing/run-incorrect-examples.sh

if [ $? -ne 0 ]; then
    echo "The following examples returned an exit status of 1"
    cat 'tools/testing/output_incorrect/incorrect_examples.txt'
    exit 1
fi
echo "Incorrect Examples return non-zero exit status"


# Everything done
echo ""
echo "--- Everything OK ---"
