BLOG=blog

LIB_FILES=lib/java_cup.jar \
 lib/JFlex-1.4.3.jar \
 lib/commons-math3-3.0.jar \
 lib/commons-math-2.2.jar \
 lib/junit-4.10.jar \
 lib/Jama.jar

MISC_FILE=compile.sh \
 makefile \
 gen_parser.sh \
 parse.sh \
 path_sep.sh \
 README \
 run.sh 

TAGNAME=$(shell git describe --exact-match --abbrev=0)
TARGETNAME=${TAGNAME}

compile:
	./compile.sh

tar: zip

zip: compile
	mkdir -p tmp/${TARGETNAME}
	cp -r src tmp/${TARGETNAME}/
	cp -r lib tmp/${TARGETNAME}/
	cp -r example tmp/${TAGNAME}/
	cp ${MISC_FILE} tmp/${TAGNAME}/
	cd tmp; zip -r ${TARGETNAME}.zip ${TARGETNAME}
	jar cf ${TARGETNAME}.jar -C bin . 
	mv tmp/${TARGETNAME}.zip ./
	rm -r -f tmp

jar: compile

demo:
	./run.sh example/poisson-ball.blog

buildparser: 
	./gen_parser.sh
	./compile

sync:
	git remote prune origin
	git pull
	git push

log:
	git log --stat --name-only --date=short --abbrev-commit > ChangeLog
