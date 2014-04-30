# PPAML challenge problem: automobile

Files in this directory:

- `automobile.blog`: the main model (generated file)
- `automobile.blog.template`: template for the main model
- `automobile_gen.py`: script to generate model from template and data
- `data`: symlink to data dir
- `DynamicsLogic.java`: logic for the dynamics function
- `TestDynamicsLogic.java`: unit test for the dynamics function
- `DynamicsInterp.java`: BLOG glue code for the dynamics function
- `LaserLogic.java`: logic for the observations function
- `TestLaserLogic.java`: unit test for the observations function
- `LaserInterp.java`: BLOG glue code for the observations function

The other files are various helpers and experiments.


## Compiling the Java components

```
cd example/ppaml_quadcopter
make
```


## Running the unit tests

```
cd example/ppaml_quadcopter
make test
```


## Generating the BLOG model from the data

```
# download the data into some directory $DATADIR
cd example/ppaml_quadcopter
ln -s $DATADIR data
python automobile_gen.py
# this will create automobile.blog
```


## Running the BLOG model

To run with MCMC:

```
cd example/ppaml_quadcopter
(cd .. && blog -s blog.sample.MHSampler -k ppaml_quadcopter ppaml_quadcopter/automobile.blog)
```

To run with Particle Filtering:

```
cd example/ppaml_quadcopter
(cd .. && blog -e blog.engine.ParticleFilter -k ppaml_quadcopter ppaml_quadcopter/automobile.blog)
```
