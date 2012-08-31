BLOG=blog

LIB_FILES=lib/java_cup.jar \
 lib/JFlex-1.4.3.jar \
 lib/commons-math3-3.0.jar \
 lib/commons-math-2.2.jar \
 lib/junit-4.10.jar \
 lib/Jama.jar

MISC_FILE=makefile \
 gen_parser.sh \
 parse.sh \
 path_sep.sh \
 README.md \
 run.sh 

TAGNAME=$(shell git tag)
TARGETNAME=TAGNAME

compile:
	./compile.sh

tar: zip

zip:
	makedir -p tmp/${TARGETNAME}
	cp src tmp/${TARGETNAME}/
	cp lib tmp/${TARGETNAME}/
	cp example tmp/${TAGNAME}/
	cp ${MISC_FILE} tmp/${TAGNAME}/
	cd tmp
	zip -r ${TARGETNAME}.zip ${TARGETNAME}
	mv ${TARGETNAME}.zip ../
	cd ..
	rm -r -f tmp

jar: compile

demo:
	./run.sh example/poisson-ball.blog

