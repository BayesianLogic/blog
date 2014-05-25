BLOG=blog

RUN_FILE=run.sh \
 run.bat \
 README.md \
 path_sep.sh

MISC_FILE=compile.sh \
 compile.bat \
 makefile \
 gen_parser.sh \
 parse.sh \
 test-ex.sh \
 ex_test_classes
 
TAGNAME=$(shell git describe --exact-match --abbrev=0 2> /dev/null)
ifneq (${TAGNAME},)
TARGETNAME=${TAGNAME}
else
TARGETNAME=blog
endif

SRC_FILES=$(shell find src -name \*.java -print)

compile: ${SRC_FILES}
	mkdir -p bin
	javac -cp "lib/*" -d bin/ ${SRC_FILES}

debug: ${SRC_FILES}
	mkdir -p bin
	javac -g -cp "lib/*" -d bin/ ${SRC_FILES}

tar: zip

zip: 
	mkdir -p tmp/${TARGETNAME}
	cp ${RUN_FILE} tmp/${TARGETNAME}/
	cp -r lib tmp/${TARGETNAME}/
	jar cfe ${TARGETNAME}.jar blog.Main -C bin . 
	mv ${TARGETNAME}.jar tmp/${TARGETNAME}/lib/
	cd tmp; zip -r ${TARGETNAME}-bin.zip ${TARGETNAME}
	rm tmp/${TARGETNAME}/lib/${TARGETNAME}.jar
	cp -r src tmp/${TARGETNAME}/
	cp -r example tmp/${TARGETNAME}/
	cp ${MISC_FILE} tmp/${TARGETNAME}/
	cd tmp; zip -r ${TARGETNAME}.zip ${TARGETNAME}
	mv tmp/${TARGETNAME}.zip ./
	mv tmp/${TARGETNAME}-bin.zip ./
	rm -r -f tmp

demo:
	./run.sh example/poisson-ball.blog

parser: src/blog/parse/BLOGLexer.flex src/blog/parse/BLOGParser.cup
	java -cp lib/JFlex-1.4.3.jar JFlex.Main src/blog/parse/BLOGLexer.flex
	cd src/blog/parse; java -cp ../../../lib/java_cup.jar java_cup.Main -symbols BLOGTokenConstants -parser BLOGParser BLOGParser.cup

sync:
	git remote prune origin
	git branch --merged | grep -v "\*" | xargs -n 1 git branch -d
	git pull
	git push

log:
	git log --stat --name-only --date=short --abbrev-commit > ChangeLog
	
test:
	./test-ex.sh
