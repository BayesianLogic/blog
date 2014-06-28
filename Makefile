BLOG=blog

RUN_FILE=blog \
 blog.bat \
 dblog \
 README.md \
 path_sep.sh \
 iblog \
 iblog.scala 
 

MISC_FILE= compile.bat \
 Makefile \
 parse.sh \
 parse.bat

CLASSPATH=lib/java-cup-11b.jar:lib/*

TAGNAME=$(shell git describe --exact-match --abbrev=0 2> /dev/null)
ifneq (${TAGNAME},)
TARGETNAME=${TAGNAME}
else
TARGETNAME=blog-$(shell git rev-parse --short HEAD)
endif

SRC_FILES=$(shell find src -name \*.java -print)

help:
	@echo 'Makefile for BLOG                                                      '
	@echo '                                                                       '
	@echo 'Usage:                                                                 '
	@echo '   make compile                     compile BLOG system                '
	@echo '   make clean                       remove the generated files         '
	@echo '   make debug                       compile the code with debug flag   '
	@echo '   make release                     create release zip files           '
	@echo '   make html                        create documentation and webpages  '
	@echo '   make demo                        run the BLOG demo                  '
	@echo '   make parser                      regenerate the parser              '
	@echo '   make sync                        clean repo and sync with remote    '
	@echo '                                                                       '


compile: ${SRC_FILES}
	mkdir -p bin
	javac -cp ${CLASSPATH} -d bin/ ${SRC_FILES}

debug: ${SRC_FILES}
	mkdir -p bin
	javac -g -cp ${CLASSPATH} -d bin/ ${SRC_FILES}

release: release-compile html zip

release-compile:
	mkdir -p bin
	javac -source 1.5 -target 1.5 -cp ${CLASSPATH} -d bin/ ${SRC_FILES}

tar: zip

zip: 
	mkdir -p tmp/${TARGETNAME}
	cp ${RUN_FILE} tmp/${TARGETNAME}/
	cp -r lib tmp/${TARGETNAME}/
	mv docs/output tmp/${TARGETNAME}/docs 		
	cp -r example tmp/${TARGETNAME}/
	jar cfe ${TARGETNAME}.jar blog.Main -C bin . 
	mv ${TARGETNAME}.jar tmp/${TARGETNAME}/lib/
	cd tmp; zip -r ${TARGETNAME}-bin.zip ${TARGETNAME}
	rm tmp/${TARGETNAME}/lib/${TARGETNAME}.jar
	cp -r src tmp/${TARGETNAME}/
	cp ${MISC_FILE} tmp/${TARGETNAME}/
	cd tmp; zip -r ${TARGETNAME}.zip ${TARGETNAME}
	mv tmp/${TARGETNAME}.zip ./
	mv tmp/${TARGETNAME}-bin.zip ./
	rm -r -f tmp

html:
	cd docs; make html

demo:
	./blog example/poisson-ball.blog

parser: src/blog/parse/BLOGLexer.flex src/blog/parse/BLOGParser.cup
	java -cp jflex-1.6.0.jar jflex.Main src/blog/parse/BLOGLexer.flex
	cd src/blog/parse; java -cp ../../../lib/java-cup-11b.jar java_cup.Main -locations -symbols BLOGTokenConstants -parser BLOGParser BLOGParser.cup

sync:
	git remote prune origin
	git branch --merged | grep -v "\*" | xargs -n 1 git branch -d
	git pull
	git push

clean:
	rm -rf bin
	rm -rf docs/output

log:
	git log --stat --name-only --date=short --abbrev-commit > ChangeLog
	
test:
	./test-ex.sh
