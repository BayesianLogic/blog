BLOG=blog

LIB_FILES=lib/java_cup.jar \
 lib/JFlex-1.4.3.jar \
 lib/commons-math3-3.0.jar \
 lib/commons-math-2.2.jar \
 lib/junit-4.10.jar \
 lib/Jama.jar

MISC_FILE=compile.sh \
 dblog.vim \
 makefile \
 gen_parser.sh \
 parse.sh \
 path_sep.sh \
 README.md \
 run.sh 

TAGNAME=$(shell git describe --exact-match --abbrev=0)
TARGETNAME=${TAGNAME}

compile:
	./compile.sh

tar: zip

zip:
	mkdir -p tmp/${TARGETNAME}
	cp -r src tmp/${TARGETNAME}/
	cp -r lib tmp/${TARGETNAME}/
	cp -r example tmp/${TAGNAME}/
	cp ${MISC_FILE} tmp/${TAGNAME}/
	cd tmp; zip -r ${TARGETNAME}.zip ${TARGETNAME}
	mv tmp/${TARGETNAME}.zip ./
	rm -r -f tmp

jar: compile

demo:
	./run.sh example/poisson-ball.blog

sync:
	git remote prune origin
	git pull
	git push

