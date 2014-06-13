# PPAML challenge problem: automobile

Directory structure:

- `data`: symlink to data dir
- `java-src`: dynamics and observation functions
- `python-src`: code for generating the model from data
- `experiments`: stuff that's in flux all the time


## Setting up your environment

All examples below assume that you have done the following:

```
cd example/ppaml_quadcopter

# Make sure data/ is a symlink to the data dir (containing 1_straight etc)

# Set up the appropriate PYTHONPATH and CLASSPATH:
source setup_env
```


## Compiling the Java components

```
cd java-src

# Compile:
make

# Run unit tests:
make test
```


## Running on a dataset

```
# Generate car.blog in the current directory:
python -m ppaml_car.blog_gen 2_bend noisy

# Run BLOG particle filter and write out.json:
blog -e blog.engine.ParticleFilter -n 100 -r -o out.json car.blog

# Evaluate results:
python -m ppaml_car.evaluate 2_bend out.json --plot
```
