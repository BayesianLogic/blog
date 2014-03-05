BLOG_HOME = "/home/cberzan/berkeley/dblog"
CLASSPATH = "${BLOG_HOME}/bin:${BLOG_HOME}/*:${BLOG_HOME}/lib/*"


all: standalone_stuff blog_glue

standalone_stuff: LaserLogic.java TestLaserLogic.java
	cd .. && javac -cp ${CLASSPATH} \
		ppaml_quadcopter/LaserLogic.java \
		ppaml_quadcopter/TestLaserLogic.java

blog_glue: LaserLogic.java TestLaserLogic.java LaserInterp.java
	cd .. && javac -cp ${CLASSPATH} \
		ppaml_quadcopter/LaserLogic.java \
		ppaml_quadcopter/TestLaserLogic.java \
		ppaml_quadcopter/LaserInterp.java

test:
	cd .. && java -cp ${CLASSPATH}:. \
		org.junit.runner.JUnitCore \
		ppaml_quadcopter.TestLaserLogic
