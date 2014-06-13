BLOG_HOME = /home/cberzan/berkeley/dblog
CLASSPATH = ${BLOG_HOME}/bin:${BLOG_HOME}/*:${BLOG_HOME}/lib/*


all: laser_standalone laser_glue dynamics_standalone dynamics_glue

laser_standalone: ppaml_car/LaserLogic.java ppaml_car/TestLaserLogic.java
	javac -cp ${CLASSPATH} \
		ppaml_car/LaserLogic.java \
		ppaml_car/TestLaserLogic.java

laser_glue: ppaml_car/LaserLogic.java ppaml_car/TestLaserLogic.java ppaml_car/LaserInterp.java
	javac -cp ${CLASSPATH} \
		ppaml_car/LaserLogic.java \
		ppaml_car/TestLaserLogic.java \
		ppaml_car/LaserInterp.java

dynamics_standalone: ppaml_car/DynamicsLogic.java ppaml_car/TestDynamicsLogic.java
	javac -cp ${CLASSPATH} \
		ppaml_car/DynamicsLogic.java \
		ppaml_car/TestDynamicsLogic.java

dynamics_glue: ppaml_car/DynamicsLogic.java ppaml_car/TestDynamicsLogic.java ppaml_car/DynamicsInterp.java
	javac -cp ${CLASSPATH} \
		ppaml_car/DynamicsLogic.java \
		ppaml_car/TestDynamicsLogic.java \
		ppaml_car/DynamicsInterp.java

test:
	java -cp ${CLASSPATH}:. \
		org.junit.runner.JUnitCore \
		ppaml_car.TestLaserLogic \
		ppaml_car.TestDynamicsLogic
