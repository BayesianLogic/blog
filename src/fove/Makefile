# Makefile for the xblog directory

CLASSPATH = ".."
JCFLAGS = -g -classpath ${CLASSPATH}

%.class : %.java
	${JAVAC} ${JCFLAGS} $<

CLASSES = LiftedVarElim.class \
	LiftedInfOperator.class \
	LiftedInfOperatorOracle.class \
	Propositionalization.class \
	CountConversion.class \
	Exponentiation.class \
	ParMarkovNet.class \
	LiftedDecisionTree.class \
	SummingOut.class \
	CountExpansion.class \
	Parfactor.class \
	ShatteredParfactorBag.class \
	Histogram.class \
	HistogramType.class \
	CountingTerm.class \
	Constraint.class \
	ElimTester.class \
	GroundQuery.class \
	NoQuery.class \

all : ${CLASSES} 

clean:
	rm -f *.class

# For each .class file, we make a list of the .java files which, if modified, 
# should trigger recompilation of this class file.  We don't need to write 
# a command (like javac) because that's handled by the suffix rule at the 
# beginning of this file.  
#
# We want to be able to add a dependency to a file by modifying just a single 
# line of the Makefile, to keep CVS from getting confused.  So we end every 
# dependency line with a \, even if it's currently the last dependency.  This 
# requires leaving a blank line after each rule (which helps readability 
# anyway).  

LiftedVarElim.class : LiftedVarElim.java \
	../blog/InferenceEngine.java \
	../blog/Model.java \
	LiftedInfOperatorOracle.java \
	ShatteredParfactorBag.java \
	ParMarkovNet.java \

Propositionalization.class : Propositionalization.java \
	LiftedVarElim.java \
	LiftedInfOperator.java \
	Parfactor.java \
	../blog/LogicalVar.java \
	../blog/Substitution.java \

CountConversion.class : CountConversion.java \
	LiftedInfOperator.java \
	Parfactor.java \
	../ve/Potential.java \
	../ve/MultiArrayPotential.java \
	../blog/LogicalVar.java \
	../blog/FuncAppTerm.java \
	../blog/Type.java \
	../common/TupleIterator.java \

Exponentiation.class : Exponentiation.java \

SummingOut.class : SummingOut.java \
	LiftedInfOperator.java \
	Parfactor.java \
	CountingTerm.java \
	../blog/Substitution.java \
	../blog/Term.java \
	../blog/FuncAppTerm.java \

CountExpansion.class : CountExpansion.java \
	LiftedVarElim.java \
	LiftedInfOperator.java \
	Parfactor.java \
	CountingTerm.java \
	Histogram.java \
	../blog/Substitution.java \
	../blog/Term.java \
	../blog/Type.java \
	../common/CartesianProduct.java \

LiftedInfOperator.class : LiftedInfOperator.java \
	ParMarkovNet.java \

LiftedInfOperatorOracle.class : LiftedInfOperatorOracle.java \
	Parfactor.java \
	ElimTester.java \
	LiftedInfOperator.java \

ParMarkovNet.class : ParMarkovNet.java \
	Parfactor.java \

LiftedDecisionTree.class : LiftedDecisionTree.java \
	../ve/DecisionTree.java

Parfactor.class : Parfactor.java \

ShatteredParfactorBag.class : ShatteredParfactorBag.java \

Constraint.class : Constraint.java \

Histogram.class : Histogram.java \

HistogramType.class : HistogramType.java \
	../blog/Type.java\

CountingTerm.class : CountingTerm.java \
	HistogramType.java \
	../blog/Term.java \
	../blog/ArgSpec.java \

ElimTester.class : ElimTester.java \

GroundQuery.class : GroundQuery.java \
	ElimTester.java \
	Constraint.java \

NoQuery.class : NoQuery.java \
	ElimTester.java \
	Constraint.java \

# Blank line below; needed by non-GNU make

