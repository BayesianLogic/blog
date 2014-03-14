BLOG_HOME = /home/cberzan/berkeley/dblog
CLASSPATH = ${BLOG_HOME}/bin:${BLOG_HOME}/*:${BLOG_HOME}/lib/*


all: laser_standalone laser_glue dynamics_standalone dynamics_glue

laser_standalone: LaserLogic.java TestLaserLogic.java
	cd .. && javac -cp ${CLASSPATH} \
		ppaml_quadcopter/LaserLogic.java \
		ppaml_quadcopter/TestLaserLogic.java

laser_glue: LaserLogic.java TestLaserLogic.java LaserInterp.java
	cd .. && javac -cp ${CLASSPATH} \
		ppaml_quadcopter/LaserLogic.java \
		ppaml_quadcopter/TestLaserLogic.java \
		ppaml_quadcopter/LaserInterp.java

dynamics_standalone: DynamicsLogic.java TestDynamicsLogic.java
	cd .. && javac -cp ${CLASSPATH} \
		ppaml_quadcopter/DynamicsLogic.java \
		ppaml_quadcopter/TestDynamicsLogic.java

dynamics_glue: DynamicsLogic.java TestDynamicsLogic.java DynamicsInterp.java
	cd .. && javac -cp ${CLASSPATH} \
		ppaml_quadcopter/DynamicsLogic.java \
		ppaml_quadcopter/TestDynamicsLogic.java \
		ppaml_quadcopter/DynamicsInterp.java

test:
	cd .. && java -cp ${CLASSPATH}:. \
		org.junit.runner.JUnitCore \
		ppaml_quadcopter.TestLaserLogic \
		ppaml_quadcopter.TestDynamicsLogic
