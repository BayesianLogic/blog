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
 

TAGNAME=$(shell git describe --exact-match --abbrev=0)
ifneq (,$(findstring fatal:,${TAGNAME}))
TARGETNAME=${TAGNAME}
else
TARGETNAME=blog
endif

try:
	@echo ${TARGETNAME}

compile:
	./compile.sh

debug:
	./compile.sh debug

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

buildparser: 
	./gen_parser.sh
	./compile.sh

sync:
	git remote prune origin
	git branch --merged | grep -v "\*" | xargs -n 1 git branch -d
	git pull
	git push

log:
	git log --stat --name-only --date=short --abbrev-commit > ChangeLog
	
test:
	./test-ex.sh
