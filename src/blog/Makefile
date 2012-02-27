# Makefile for the blog directory

CLASSPATH = "..${J_PATH_SEP}../CUP${J_PATH_SEP}../junit.jar"
JCFLAGS = -g -classpath ${CLASSPATH}
JFLAGS = -cp ${CLASSPATH}

%.class : %.java 
	${JAVAC} ${JCFLAGS} $<

%.java : %.lex 
	${JAVA} ${JFLAGS} JLex.Main $< --package blog --class $* -o $*.java


CLASSES = ArgSpec.class \
	AtomicFormula.class \
	BLOGLexer.class \
	BLOGParser.class \
	BLOGTokenConstants.class \
	Clause.class \
	CondProbDistrib.class \
	ConjFormula.class \
	DependencyModel.class \
	DisjFormula.class \
	ImplicFormula.class \
	EqualityFormula.class \
	EqualsCPD.class \
	Evidence.class \
	ExplicitSetSpec.class \
	Formula.class \
	FormulaQuery.class \
	FuncAppTerm.class \
	ValueEvidenceStatement.class \
	Function.class \
	ImplicitSetSpec.class \
	Model.class \
	NegFormula.class \
	NonRandomFunction.class \
	OriginFunction.class \
	NonGuaranteedObject.class \
	POP.class \
	Query.class \
	AbstractQuery.class \
	RandomFunction.class \
	SymbolEvidenceStatement.class \
	SkolemConstant.class \
	Term.class \
	ArgSpecQuery.class \
	TupleSetSpec.class \
	BasicVar.class \
	NumberVar.class \
	EnumeratedObject.class \
	TrueFormula.class \
	Sampler.class \
	MHSampler.class \
	DMHSampler.class \
	DecayedProposer.class \
	Particle.class \
	ParticleFilter.class \
	ParticleFilterTest.class \
	LWSampler.class \
	Proposer.class \
	Main.class \
	BayesNetVar.class \
	DerivedVar.class \
	CardinalitySpec.class \
	PartialWorld.class \
	PartialWorldDiff.class \
	GenericProposer.class \
	AbstractPartialWorld.class \
	DefaultPartialWorld.class \
	UniversalFormula.class \
	ExistentialFormula.class \
	Type.class \
	MatrixType.class \
	ObjectIterator.class \
	ObjectIdentifier.class \
	RandFuncAppVar.class \
	OriginVar.class \
	ParserUtilities.class \
	ObjGenGraph.class \
	CompiledSetSpec.class \
	UrnBallsSplitMerge.class \
	UrnBallsSplitMergeNoIds.class \
	InferenceEngine.class \
	SamplingEngine.class \
	Timestep.class \
	BuiltInTypes.class \
	BuiltInFunctions.class \
	RejectionSampler.class \
	WorldInProgress.class \
	DefaultObjectIterator.class \
	DefaultObjectSet.class \
	FunctionInterp.class \
	ConstantInterp.class \
	SetupExtender.class \
	EvalContext.class \
	DefaultEvalContext.class \
	ParentRecEvalContext.class \
	InstantiatingEvalContext.class \
	POPAppBasedSet.class \
	AbstractFunctionInterp.class \
	AbstractObjectSet.class \
	AbstractObjectIterator.class \
	LogicalVar.class \
	SymbolTerm.class \
	GenericPOPAppSatisfier.class \
	WorldListener.class \
	WorldDiffListener.class \
	VarWithDistrib.class \
	DetCondProbDistrib.class \
	Substitution.class \
	SimpleInstEvalContext.class \

GEN_FILES = BLOGLexer.java \
	BLOGParser.java \
	BLOGTokenConstants.java \


all : ${GEN_FILES} ${CLASSES} distrib_dir

distrib_dir:
	cd distrib; make JAVAC=${JAVAC} JAVA=${JAVA} all

clean: 
	rm -f *.class
	rm -f ${GEN_FILES}
	cd distrib; make clean

# Rule for making the parser
BLOGParser.java : BLOGParser.cup 
	${JAVA} ${JFLAGS} java_cup.Main -parser BLOGParser -package blog \
		-symbols BLOGTokenConstants BLOGParser.cup

# Making BLOGParser.java makes BLOGTokenConstants.java as side effect
BLOGTokenConstants.java : BLOGParser.cup
	${JAVA} ${JFLAGS} java_cup.Main -parser BLOGParser -package blog \
		-symbols BLOGTokenConstants BLOGParser.cup


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



ArgSpec.class : ArgSpec.java \
	PartialWorld.java \

AtomicFormula.class : AtomicFormula.java \
	Formula.java \
	Term.java \
	PartialWorld.java \
	BuiltInTypes.java \

BLOGLexer.class : BLOGLexer.java \
	BLOGTokenConstants.java \

BLOGLexer.java : BLOGLexer.lex \
	BLOGTokenConstants.java \

BLOGParser.class : BLOGParser.java \
	ImplicitSetSpec.java \
	ExplicitSetSpec.java \
	TupleSetSpec.java \
	BLOGLexer.java \
	AtomicFormula.java \
	Clause.java \
	CondProbDistrib.java \
	ConjFormula.java \
	DependencyModel.java \
	DisjFormula.java \
	ImplicFormula.java \
	EqualityFormula.java \
	EqualsCPD.java \
        Evidence.java \
	Formula.java \
	FuncAppTerm.java \
	ValueEvidenceStatement.java \
	Function.java \
	Model.java \
	NegFormula.java \
	NonRandomFunction.java \
	OriginFunction.java \
	POP.java \
	RandomFunction.java \
	SkolemConstant.java \
	SymbolEvidenceStatement.java \
	Term.java \
	../common/Util.java \
	ConstantInterp.java \
	EnumeratedObject.java \
	TrueFormula.java \
	Type.java \
	BuiltInTypes.java \
	CardinalitySpec.java \
	ParserUtilities.java \
	../common/HashMapDiff.java \

BLOGTokenConstants.class : BLOGTokenConstants.java \

BoolConst.class : BoolConst.java \

Clause.class : Clause.java \
	Formula.java \
	CondProbDistrib.java \

CondProbDistrib.class : CondProbDistrib.java \

ConjFormula.class : ConjFormula.java \
	Formula.java \
	PartialWorld.java \
	DisjFormula.java \
	NegFormula.java \

DependencyModel.class : DependencyModel.java \
	Clause.java \
	Formula.java \
	PartialWorld.java \

DisjFormula.class : DisjFormula.java \
	Formula.java \
	PartialWorld.java \
	ConjFormula.java \
	NegFormula.java \

ImplicFormula.class : ImplicFormula.java \
	Formula.java \
	PartialWorld.java \
	ConjFormula.java \
	DisjFormula.java \
	NegFormula.java \

EqualityFormula.class : EqualityFormula.java \
	PartialWorld.java \
	Formula.java \

EqualsCPD.class : EqualsCPD.java \
	DetCondProbDistrib.java \

Evidence.class : Evidence.java \
	SymbolEvidenceStatement.java \
	SkolemConstant.java \
        ValueEvidenceStatement.java \
	ArgSpec.java \
	DerivedVar.java \
	DependencyModel.java \

ExplicitSetSpec.class : ExplicitSetSpec.java \
	ArgSpec.java \
	Formula.java \
	Term.java \
	PartialWorld.java \
	../common/Multiset.java \
	../common/HashMultiset.java \

Formula.class : Formula.java \
	PartialWorld.java \

FormulaQuery.class : FormulaQuery.java \
	Query.java \
	Formula.java \

FuncAppTerm.class : FuncAppTerm.java \
	Term.java \
	Function.java \
	PartialWorld.java \
	Model.java \
	BuiltInFunctions.java \

ValueEvidenceStatement.class : ValueEvidenceStatement.java \
	FuncAppTerm.java \
	NonRandomFunction.java \
	EqualityFormula.java \
	DerivedVar.java \
	BayesNetVar.java \
	../common/Util.java \

Function.class : Function.java \
	BuiltInTypes.java \

GuaranteedObject.class : GuaranteedObject.java \

ImplicitSetSpec.class : ImplicitSetSpec.java \
	ArgSpec.java \
	Term.java \
	PartialWorld.java \
	Formula.java \
	CompiledSetSpec.java \
	../common/Multiset.java \
	../common/SetBackedMultiset.java \
	../common/MultisetBackedSet.java \
	../common/HashMapDiff.java \

Model.class : Model.java \
	Function.java \
	RandomFunction.java \
	NonRandomFunction.java \
	OriginFunction.java \
	Formula.java \
	POP.java \
	SkolemConstant.java \
	Type.java \
	BuiltInTypes.java \
	../common/TupleIterator.java \

NegFormula.class : NegFormula.java \
	Formula.java \
	PartialWorld.java \

NonRandomFunction.class : NonRandomFunction.java \
	Function.java \
	Model.java \
	FunctionInterp.java \
	ConstantInterp.java \

OriginFunction.class : OriginFunction.java \
	NonGuaranteedObject.java \
	NonRandomFunction.java \
	Function.java \

NonGuaranteedObject.class : NonGuaranteedObject.java \
	NumberVar.java \
	POP.java \

POP.class : POP.java \
	DependencyModel.java \

Query.class : Query.java \
	Formula.java \
	Term.java \

AbstractQuery.class : AbstractQuery.java \

RandomFunction.class : RandomFunction.java \
	Function.java \
	DependencyModel.java \

SkolemConstant.class : SkolemConstant.java \
	Function.java \
	distrib/UniformChoice.java \

SymbolEvidenceStatement.class : SymbolEvidenceStatement.java \
	ImplicitSetSpec.java \

sym.class : sym.java \
	BLOGModelParser.java \
        BLOGEvidenceParser.java \

Term.class : Term.java \
	Formula.java \
	PartialWorld.java \

ArgSpecQuery.class : ArgSpecQuery.java \
	Query.java \
	ArgSpec.java \
	../common/Histogram.java \

ParserUtilities.class : ParserUtilities.java \
	BLOGTokenConstants.java \

TupleSetSpec.class : TupleSetSpec.java \
	ArgSpec.java \
	Formula.java \
	ConjFormula.java \
	Term.java \
	PartialWorld.java \
	ObjGenGraph.java \
	../common/AbstractTupleIterator.java \
	../common/Multiset.java \
	../common/HashMultiset.java \
	../common/HashMapDiff.java \

BasicVar.class : BasicVar.java \
	Model.java \
	../common/Util.java \

NumberVar.class : NumberVar.java \
	BasicVar.java \
	POP.java \
	Function.java \
	PartialWorld.java \
	BuiltInTypes.java \

EnumeratedObject.class : EnumeratedObject.java \

TrueFormula.class : TrueFormula.java \
	Formula.java \
	PartialWorld.java \

MHSampler.class : MHSampler.java \
	Sampler.java \
	Model.java \
	Evidence.java \
	Query.java \
	Proposer.java \
	PartialWorld.java \
	PartialWorldDiff.java \
	BasicVar.java \
	../common/Util.java \

DMHSampler.class : DMHSampler.java \
	Sampler.java \
	Model.java \
	Evidence.java \
	Query.java \
	Proposer.java \
	PartialWorld.java \
	PartialWorldDiff.java \
	BasicVar.java \
	../common/Util.java \

LWSampler.class : LWSampler.java \
	Sampler.java \
	Model.java \
	Evidence.java \
	Query.java \
	PartialWorld.java \
	PartialWorldDiff.java \
	BasicVar.java \
	InstantiatingEvalContext.java \

GenericProposer.class : GenericProposer.java \
	Proposer.java \
	PartialWorldDiff.java \
	LWSampler.java \
	../common/Util.java \
	../common/MultiMap.java \
	../common/HashMultiMap.java \

DecayedProposer.class : DecayedProposer.java \
	Proposer.java \
	PartialWorldDiff.java \
	LWSampler.java \
	../common/Util.java \
	../common/MultiMap.java \
	../common/HashMultiMap.java \

ParticleFilter.class : ParticleFilter.java \
	InferenceEngine.java \
	Model.java \
	Evidence.java \
	Query.java \
	Sampler.java \
	../common/Util.java \
	../common/Timer.java \

ParticleFilterTest.class : ParticleFilterTest.java \
	../common/Util.java \
	../common/Histogram.java \
	../common/UnaryPredicate.java \

Particle.class : Particle.java \
	Model.java \
	Evidence.java \
	Query.java \
	PartialWorld.java \
	PartialWorldDiff.java \
	BasicVar.java \

Proposer.class : Proposer.java \
	PartialWorldDiff.java \

Main.class : Main.java \
	Model.java \
	Evidence.java \
	Query.java \
	BLOGParser.java \
	Sampler.java \
	../common/Util.java \

BayesNetVar.class : BayesNetVar.java \
	PartialWorld.java \

DerivedVar.class : DerivedVar.java \
	BayesNetVar.java \
	PartialWorld.java \
	ArgSpec.java \

CardinalitySpec.class : CardinalitySpec.java \
	ArgSpec.java \
	PartialWorld.java \
	ImplicitSetSpec.java \

PartialWorld.class : PartialWorld.java \
	BayesNetVar.java \
	BasicVar.java \

PartialWorldDiff.class : PartialWorldDiff.java \
        AbstractPartialWorld.java \
	PartialWorld.java \
	BayesNetVar.java \
	BasicVar.java \
	../common/MapDiff.java \
	../common/HashMapDiff.java \
	../common/MultiMap.java \
	../common/HashMultiMap.java \
	../common/MultiMapDiff.java \
	../common/IndexedMultiMapDiff.java \
	../common/MapWithPreimages.java \
	../common/MapWithPreimagesDiff.java \
	../common/DGraph.java \
	../common/ParentUpdateDGraph.java \

AbstractPartialWorld.class : AbstractPartialWorld.java \
	PartialWorld.java \
	BasicVar.java \
	BayesNetVar.java \
	OriginVar.java \
	POP.java \
	Type.java \
	../common/MultiMap.java \
	../common/IndexedMultiMap.java \
	../common/MapWithPreimages.java \
	../common/DGraph.java \

DefaultPartialWorld.class : DefaultPartialWorld.java \
	AbstractPartialWorld.java \
	PartialWorld.java \
	../common/MultiMap.java \
	../common/HashMultiMap.java \
	../common/IndexedMultiMap.java \
	../common/IndexedHashMultiMap.java \
	../common/MapWithPreimages.java \
	../common/HashMapWithPreimages.java \
	../common/DGraph.java \
	../common/DefaultDGraph.java \

UniversalFormula.class : UniversalFormula.java \
	Formula.java \
	PartialWorld.java \
	CompiledSetSpec.java \
	../common/HashMapDiff.java \

ExistentialFormula.class : ExistentialFormula.java \
	Formula.java \
	PartialWorld.java \
	CompiledSetSpec.java \
	../common/HashMapDiff.java \

Type.class : Type.java \
	POP.java \
	Function.java \
	OriginFunction.java \

MatrixType.class : MatrixType.java \
	Type.java \
	BuiltInTypes.java \
	NonRandomFunction.java \
	FunctionInterp.java \

ObjectIterator.class : ObjectIterator.java \

ObjectIdentifier.class : ObjectIdentifier.java \
	Type.java \

RandFuncAppVar.class : RandFuncAppVar.java \
	BasicVar.java \
	Function.java \
	RandomFunction.java \

OriginVar.class : OriginVar.java \
	BayesNetVar.java \
	PartialWorld.java \

ObjGenGraph.class : ObjGenGraph.java \
	Type.java \
	POP.java \
	NonRandomFunction.java \
	Formula.java \
	Term.java \
	FuncAppTerm.java \
	AtomicFormula.java \
	EqualityFormula.java \
	PartialWorld.java \
	NumberVar.java \
	NonGuaranteedObject.java \
	ObjectIterator.java \
	BuiltInTypes.java \
	BuiltInFunctions.java \
	../common/Util.java \
	../common/DGraph.java \
	../common/AbstractDGraph.java \
	../common/TupleIterator.java \

CompiledSetSpec.class : CompiledSetSpec.java \
	Type.java \
	Formula.java \
	PartialWorld.java \
	ObjGenGraph.java \
	ConjFormula.java \
	ObjectIterator.java \

UrnBallsSplitMerge.class : UrnBallsSplitMerge.java \
	PartialWorld.java \
	PartialWorldDiff.java \
	Type.java \
	RandFuncAppVar.java \
	BasicVar.java \
	BayesNetVar.java \
	CondProbDistrib.java \
	DependencyModel.java \
	../common/Util.java \

UrnBallsSplitMergeNoIds.class : UrnBallsSplitMergeNoIds.java \
	PartialWorld.java \
	PartialWorldDiff.java \
	Type.java \
	RandFuncAppVar.java \
	BasicVar.java \
	BayesNetVar.java \
	CondProbDistrib.java \
	DependencyModel.java \
	NonGuaranteedObject.java \
	../common/Util.java \

InferenceEngine.class : InferenceEngine.java \
	Model.java \
	Evidence.java \
	Query.java \

SamplingEngine.class : SamplingEngine.java \
	InferenceEngine.java \
	Model.java \
	Evidence.java \
	Query.java \
	Sampler.java \
	../common/Util.java \
	../common/Timer.java \

Timestep.class : Timestep.java \

BuiltInTypes.class : BuiltInTypes.java \
	Type.java \
	MatrixType.java \

BuiltInFunctions.class : BuiltInFunctions.java \
	BuiltInTypes.java \
	Model.java \

RejectionSampler.class : RejectionSampler.java \
	Sampler.java \
	Evidence.java \
	Query.java \
	WorldInProgress.java \
	BasicVar.java \
	CondProbDistrib.java \

WorldInProgress.class : WorldInProgress.java \
	PartialWorld.java \
	AbstractPartialWorld.java \
	DefaultPartialWorld.java \
	BasicVar.java \
	NumberVar.java \
	RandFuncAppVar.java \
	Function.java \
	RandomFunction.java \
	POP.java \
	NonGuaranteedObject.java \
	../common/ExtensibleLinkedList.java \
	../common/AddedTupleIterator.java \

DefaultObjectIterator.class : DefaultObjectIterator.java \
	ObjectIterator.java \

DefaultObjectSet.class : DefaultObjectSet.java \
	AbstractObjectSet.java \
	ObjectSet.java \
	DefaultObjectIterator.java \
	../common/IndexedSet.java \
	../common/IndexedHashSet.java \

FunctionInterp.class : FunctionInterp.java \

ConstantInterp.class : ConstantInterp.java \
	FunctionInterp.java \

SetupExtender.class : SetupExtender.java \

EvalContext.class : EvalContext.java \

DefaultEvalContext.class : DefaultEvalContext.java \
	EvalContext.java \
	../common/Util.java \

ParentRecEvalContext.class : ParentRecEvalContext.java \
	EvalContext.java \
	DefaultEvalContext.java \

InstantiatingEvalContext.class : InstantiatingEvalContext.java \
	EvalContext.java \
	DefaultEvalContext.java \
	ParentRecEvalContext.java \
	VarWithDistrib.java \

POPAppBasedSet.class : POPAppBasedSet.java \
	ObjectSet.java \
	AbstractObjectSet.java \
	NumberVar.java \

AbstractFunctionInterp.class : AbstractFunctionInterp.java \
	FunctionInterp.java \

AbstractObjectSet.class : AbstractObjectSet.java \
	ObjectSet.java \

AbstractObjectIterator.class : AbstractObjectIterator.java \
	ObjectIterator.java \

LogicalVar.class : LogicalVar.java \

SymbolTerm.class : SymbolTerm.java \
	Term.java \
	ArgSpec.java \
	Model.java \
	Function.java \
	LogicalVar.java \

GenericObject.class : GenericObject.java \

GenericPOPAppSatisfier.class : GenericPOPAppSatisfier.java \
	GenericObject.java \
	POP.java \
	NumberVar.java \
	EvalContext.java \

WorldDiffListener.class : WorldDiffListener.java \

WorldListener.class : WorldListener.java \

VarWithDistrib.class : VarWithDistrib.java \
	BasicVar.java \
	EvalContext.java \
	InstantiatingEvalContext.java \

DetCondProbDistrib.class : DetCondProbDistrib.java \
	AbstractCondProbDistrib.java \
	CondProbDistrib.java \

Substitution.class : Substitution.java \

SimpleInstEvalContext.class : SimpleInstEvalContext.java \
	ParentRecEvalContext.java \
	EvalContext.java \
	RandFuncAppVar.java \
	Type.java \

# blank line below; needed by non-GNU make



