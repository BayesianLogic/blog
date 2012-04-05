# Makefile for the common directory

CLASSPATH = "..${J_PATH_SEP}../CUP"
JCFLAGS = -g -classpath ${CLASSPATH}

%.class : %.java 
	${JAVAC} ${JCFLAGS} $<

CLASSES = Bernoulli.class \
	BinarySequenceDistrib.class \
	BipartiteMatcher.class \
	DefaultReferenceHistogram.class \
	FilteredIterator.class \
	FilteredMapIterator.class \
	Geometric.class \
	IntegerDist.class \
	Multinomial.class \
	MultinomialWithTail.class \
	PermutationDistrib.class \
	PermutationIterator.class \
	Permutation.class \
	Poisson.class \
	ReferenceHistogram.class \
	ReferenceHistogramPair.class \
	RoundedLogNormal.class \
	SubsetIterator.class \
	SubstringReplacer.class \
	Timer.class \
	Util.class \
	UnaryFunction.class \
	UnaryPredicate.class \
	NullaryFunction.class \
	Multiset.class \
	AbstractMultiset.class \
	HashMultiset.class \
	SetBackedMultiset.class \
	MultisetBackedSet.class \
	Histogram.class \
	TabularReader.class \
	MapWithPreimages.class \
	AbstractMapWithPreimages.class \
	HashMapWithPreimages.class \
	MapWithPreimagesDiff.class \
	PrecisionRecall.class \
	DGraph.class \
	AbstractDGraph.class \
	DefaultDGraph.class \
	ParentUpdateDGraph.class \
	DefaultMapEntry.class \
	MultiMap.class \
	HashMultiMap.class \
	SetDiff.class \
	HashSetDiff.class \
	MapDiff.class \
	HashMapDiff.class \
	MultiMapDiff.class \
	HashMultiMapDiff.class \
	WeakHashSet.class \
	ExtensibleLinkedList.class \
	AddedTupleIterator.class \
	LocationInFile.class \
	IndexedSet.class \
	IndexedHashSet.class \
	IndexedSetDiff.class \
	IndexedMultiMap.class \
	IndexedHashMultiMap.class \
	IndexedMultiMapDiff.class \
	Heap.class \
	BinaryHeap.class \
	FibHeap.class \
	SamplingMultiset.class \
	SetWithDistrib.class \
	LogHistogram.class \
	IndexedSortedSet.class \
	IndexedTreeSet.class \
	IndexedTreeSetDiff.class \
	IndexedHashSetDiff.class \
	SparseVector.class \
	DenseVector.class \
	ImplicitVector.class \
	ScannerWithLocInfo.class \
	CartesianProduct.class \
	cmdline/Parser.class \
	cmdline/Option.class \
	cmdline/AbstractOption.class \
	cmdline/BooleanOption.class \
	cmdline/IntOption.class \
	cmdline/DoubleOption.class \
	cmdline/StringOption.class \
	cmdline/StringListOption.class \
	cmdline/PropertiesOption.class \

all : ${CLASSES} 

clean: 
	rm -f *.class cmdline/*.class

# For each .class file, we make a list of the .class files which, if modified, 
# should trigger recompilation of this class file.  We don't need to write 
# a command (like javac) because that's handled by the suffix rule at the 
# beginning of this file.  
#
# We want to be able to add a dependency to a file by modifying just a single 
# line of the Makefile, to keep CVS from getting confused.  So we end every 
# dependency line with a \, even if it's currently the last dependency.  This 
# requires leaving a blank line after each rule (which helps readability 
# anyway).  

Bernoulli.class: Bernoulli.java \

BinarySequenceDistrib.class: BinarySequenceDistrib.java \

BipartiteMatcher.class : BipartiteMatcher.java \
	Util.java \

DefaultReferenceHistogram.class : DefaultReferenceHistogram.java \
	ReferenceHistogram.java \

FilteredIterator.class : FilteredIterator.java \

FilteredMapIterator.class : FilteredMapIterator.java \
	FilteredIterator.java \

Geometric.class : Geometric.java \
	IntegerDist.java \

IntegerDist.class : IntegerDist.java \

Multinomial.class : Multinomial.java \
	IntegerDist.java \
	Util.java \

MultinomialWithTail.class : MultinomialWithTail.java \
	IntegerDist.java \
	Multinomial.java \
	Geometric.java \

PermutationDistrib.class : PermutationDistrib.java \
	Permutation.java \

PermutationIterator.class : PermutationIterator.java \

Permutation.class : Permutation.java \

Poisson.class : Poisson.java \
	Util.java \

ReferenceHistogram.class : ReferenceHistogram.java \
	DefaultReferenceHistogram.java \

ReferenceHistogramPair.class : ReferenceHistogramPair.java \
	ReferenceHistogram.java \
	DefaultReferenceHistogram.java \

RoundedLogNormal.class : RoundedLogNormal.java \

SubsetIterator.class : SubsetIterator.java \

SubstringReplacer.class : SubstringReplacer.java \
	Util.java \

Timer.class : Timer.java \

Util.class : Util.java \

UnaryFunction.class : UnaryFunction.java \

UnaryPredicate.class : UnaryPredicate.java \

NullaryFunction.class : NullaryFunction.java \

Multiset.class : Multiset.java \

AbstractMultiset.class : AbstractMultiset.java \

HashMultiset.class : HashMultiset.java \
	Multiset.java \
	AbstractMultiset.java \

SetBackedMultiset.class : SetBackedMultiset.java \
	Multiset.java \
	AbstractMultiset.java \

MultisetBackedSet.class : MultisetBackedSet.java \
	Multiset.java \

Histogram.class : Histogram.java \

TabularReader.class : TabularReader.java \

MapWithPreimages.class : MapWithPreimages.java \

AbstractMapWithPreimages : AbstractMapWithPreimages.java \
	MapWithPreimages.java \
	MultiMap.java \

HashMapWithPreimages.class : HashMapWithPreimages.java \
	AbstractMapWithPreimages.java \
	MapWithPreimages.java \
	MultiMap.java \
	HashMultiMap.java \

MapWithPreimagesDiff.class : MapWithPreimagesDiff.java \
	AbstractMapWithPreimages.java \
	MapDiff.java \
	HashMapDiff.java \
	MultiMapDiff.java \
	HashMultiMapDiff.java \

PrecisionRecall.class : PrecisionRecall.java \

DGraph.class : DGraph.java \

AbstractDGraph.class : AbstractDGraph.java \

DefaultDGraph.class : DefaultDGraph.java \
	AbstractDGraph.java \
	DGraph.java \

ParentUpdateDGraph.class : ParentUpdateDGraph.java \
	AbstractDGraph.java \
	DGraph.java \
	MultiMap.java \
	HashMultiMap.java \

DefaultMapEntry.class : DefaultMapEntry.java \

MultiMap.class : MultiMap.java \

HashMultiMap.class : HashMultiMap.java \
	MultiMap.java \

SetDiff.class : SetDiff.java \

HashSetDiff.class : HashSetDiff.java \
	SetDiff.java

MapDiff.class : MapDiff.java \

HashMapDiff.class : HashMapDiff.java \
	MapDiff.java \
	DefaultMapEntry.java \

MultiMapDiff.class : MultiMapDiff.java \
	MultiMap.java \

HashMultiMapDiff.class : HashMultiMapDiff.java \
	MultiMapDiff.java \
	MultiMap.java \
	HashMultiMap.java \
	DefaultMapEntry.java \

WeakHashSet.class: WeakHashSet.java \

ExtensibleLinkedList.class : ExtensibleLinkedList.java \

AddedTupleIterator.class : AddedTupleIterator.java \

LocationInFile.class : LocationInFile.java \

IndexedSet.class : IndexedSet.java \

IndexedHashSet.class : IndexedHashSet.java \
	IndexedSet.java \

IndexedSetDiff.class : IndexedSetDiff.java \
	IndexedSet.java \
	IndexedHashSet.java \

IndexedMultiMap.class : IndexedMultiMap.java \
	MultiMap.java \

IndexedHashMultiMap.class : IndexedHashMultiMap.java \
	HashMultiMap.java \
	MultiMap.java \
	IndexedMultiMap.java \
	IndexedSet.java \

IndexedMultiMapDiff.class : IndexedMultiMapDiff.java \
	MultiMapDiff.java \
	MultiMap.java \
	IndexedMultiMap.java \
	IndexedSet.java \
	IndexedSetDiff.java \

BinaryHeap.class : BinaryHeap.java \
	Util.java \

FibHeap.class : FibHeap.java \
	Util.java \

SamplingMultiset.class : SamplingMultiset.java \
	Util.java \

SparseVector.class : SparseVector.java \
	ImplicitVector.java \
	Util.java \

DenseVector.class : DenseVector.java \
	ImplicitVector.java \

ImplicitVector.class : ImplicitVector.java \

ScannerWithLocInfo.class : ScannerWithLocInfo.java \

CartesianProduct.class : CartesianProduct.java \

cmdline/Parser.class : cmdline/Parser.java \
	cmdline/Option.java \

cmdline/Option.class: cmdline/Option.java \

cmdline/AbstractOption.class : cmdline/AbstractOption.java \
	cmdline/Option.java \

cmdline/BooleanOption.class : cmdline/BooleanOption.java \
	cmdline/Option.java \
	cmdline/AbstractOption.java \

cmdline/IntOption.class : cmdline/IntOption.java \
	cmdline/Option.java \
	cmdline/AbstractOption.java \

cmdline/DoubleOption.class : cmdline/DoubleOption.java \
	cmdline/Option.java \
	cmdline/AbstractOption.java \

cmdline/StringOption.class : cmdline/StringOption.java \
	cmdline/Option.java \
	cmdline/AbstractOption.java \

cmdline/StringListOption.class : cmdline/StringListOption.java \
	cmdline/Option.java \
	cmdline/AbstractOption.java \

cmdline/PropertiesOption.class : cmdline/PropertiesOption.java \
	cmdline/Option.java \
	cmdline/AbstractOption.java \


# blank line below; needed by non-GNU make
