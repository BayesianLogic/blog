# Makefile for the ve directory

CLASSPATH = "..${J_PATH_SEP}../CUP"
JCFLAGS = -g -classpath ${CLASSPATH}
JFLAGS = -cp ${CLASSPATH}

%.class : %.java 
	${JAVAC} ${JCFLAGS} $<

%.java : %.lex 
	${JAVA} ${JFLAGS} JLex.Main $< --package blog --class $* -o $*.java


CLASSES = MultiArray.class \
	DimMapping.class \
	Potential.class \
	MultiArrayPotential.class \
	DecisionTree.class \
	Factor.class \
	MarkovNet.class \
	VarElimEngine.class \

all : ${CLASSES} 

clean: 
	rm -f *.class
	rm -f ${GEN_FILES}

# should trigger recompilation of this class file.  We don't need to write 
# a command (like javac) because that's handled by the suffix rule at the 
# beginning of this file.  
#
# We want to be able to add a dependency to a file by modifying just a single 
# line of the Makefile, to keep CVS from getting confused.  So we end every 
# dependency line with a \, even if it's currently the last dependency.  This 
# requires leaving a blank line after each rule (which helps readability 
# anyway).  

MultiArray.class : MultiArray.java \

DimMapping.class : DimMapping.java \

Potential.class : Potential.java \

MultiArrayPotential.class : MultiArrayPotential.java \
	MultiArray.java \

DecisionTree.class : DecisionTree.java \

Factor.class : Factor.java \
	Potential.java \

MarkovNet.class : MarkovNet.java \
	Factor.java \
	../fove/ParMarkovNet.java \
	../fove/Parfactor.java \

VarElimEngine.class : VarElimEngine.java \
	MarkovNet.java \

# blank line below; needed by non-GNU make



