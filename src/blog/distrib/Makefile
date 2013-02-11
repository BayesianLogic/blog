# Makefile for the blog/distrib directory

CLASSPATH = "../.."
JCFLAGS = -g -classpath ${CLASSPATH}

%.class : %.java 
	${JAVAC} ${JCFLAGS} $<


CLASSES = Bernoulli.class \
	UniformInt.class \
	UniformChoice.class \
	Poisson.class \
	TabularCPD.class \
	BinaryBernoulliDistrib.class \
	UniformVector.class \
	UnivarGaussian.class \
	Gaussian.class \
	MultivarGaussian.class \
	Geometric.class \
	ListInterp.class \
	TabularInterp.class \
	LinearGaussian.class \
	VectorInRect.class \
	Categorical.class \
	Exponential.class \
	Gamma.class \
	Beta.class \
	Binomial.class \
	NegativeBinomial.class \
	UniformReal.class \
	RoundedLogNormal.class \
	NatNumDistribWithTail.class \
	StringEditModel.class \
	StringEditModelWithJumps.class \
	CharDistrib.class \
	Iota.class \
	ChooseFromArgs.class \
	BoundedGeometric.class \
	MixtureDistrib.class \

all : ${CLASSES}


clean: 
	rm -f *.class


Bernoulli.class : Bernoulli.java \
	../CondProbDistrib.java \
	../../common/Util.java \

BinaryBernoulliDistrib.class : BinaryBernoulliDistrib.java \
	../CondProbDistrib.java \
	../../common/Util.java \

UniformVectorDistrib.class : UniformVectorDistrib.java \
	../CondProbDistrib.java \
	../../common/Util.java \

UnivarGaussian.class : UnivarGaussian.java \
	../CondProbDistrib.java \
	../../common/Util.java \

Gaussian.class : Gaussian.java \
	UnivarGaussian.java \
	../CondProbDistrib.java \

MultivarGaussian.class : MultivarGaussian.java \
	UnivarGaussian.java \
	../CondProbDistrib.java \
	../../common/Util.java \

Geometric.class : Geometric.java \
	../CondProbDistrib.java \
	../../common/Util.java \

UniformInt.class : UniformInt.java \
	../CondProbDistrib.java \
	../../common/Util.java \

UniformChoice.class : UniformChoice.java \
	../CondProbDistrib.java \
	../ObjectSet.java \
	../../common/Util.java \
	../../common/Multiset.java \

Poisson.class : Poisson.java \
	../CondProbDistrib.java \

TabularCPD.class : TabularCPD.java \
	../CondProbDistrib.java \
	../Model.java \
	../EnumeratedObject.java \
	../BuiltInTypes.java \
	../../common/Util.java \

ListInterp.class : ListInterp.java \
	../FunctionInterp.java \

TabularInterp.class : TabularInterp.java \
	../FunctionInterp.java \
	../Model.java \

LinearGaussian.class : LinearGaussian.java \
	MultivarGaussian.java \
	../CondProbDistrib.java \
	../AbstractCondProbDistrib.java \
	../../common/Util.java \

VectorInRect.class : VectorInRect.java \
	../FunctionInterp.java \
	../../Jama/Matrix.java \

Categorical.class : Categorical.java \
	../CondProbDistrib.java \
	../Model.java \
	../Type.java \
	../../common/Util.java \
	../../Jama/Matrix.java \

Exponential.class : Exponential.java \
	../CondProbDistrib.java \
	../../common/Util.java \

Gamma.class : Gamma.java \
	../CondProbDistrib.java \
	../../common/Util.java \

Beta.class : Beta.java \
	Gamma.java \
	../CondProbDistrib.java \
	../../common/Util.java \

Binomial.class : Binomial.java \
	../CondProbDistrib.java \
	../../common/Util.java \

NegativeBinomial.class : NegativeBinomial.java \
	Gamma.java \
	Poisson.java \
	../CondProbDistrib.java \
	../../common/Util.java \

UniformReal.class : UniformReal.java \
	../CondProbDistrib.java \
	../../common/Util.java \

RoundedLogNormal.class : RoundedLogNormal.java \
	UnivarGaussian.java \
	../CondProbDistrib.java \
	../../common/Util.java \

NatNumDistribWithTail.class : NatNumDistribWithTail.java \
	Bernoulli.java \
	Categorical.java \
	Geometric.java \
	../CondProbDistrib.java \
	../../common/Util.java \

StringEditModel.class : StringEditModel.java \
	CharDistrib.java \
	../CondProbDistrib.java \
	../../common/Util.java \

StringEditModelWithJumps.class : StringEditModelWithJumps.java \
	StringEditModel.java \

CharDistrib.class : CharDistrib.java \
	Categorical.java \
	../CondProbDistrib.java \

Iota.class : Iota.java \
	../DetCondProbDistrib.java \

ChooseFromArgs.class : ChooseFromArgs.java \
	../CondProbDistrib.java \
	../../common/Util.java \

BoundedGeometric.class : BoundedGeometric.java \
	../CondProbDistrib.java \
	../../common/Util.java \

MixtureDistrib.class : MixtureDistrib.java \
	Categorical.java \
	../CondProbDistrib.java \
	../../common/Util.java \

# blank line below; needed by non-GNU make
