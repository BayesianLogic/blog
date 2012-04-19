#!/bin/bash
java -cp lib/JFlex-1.4.3.jar JFlex.Main src/blog/parse/BLOGLexer.flex
cd src/blog/parse
java -cp ../../../lib/java_cup.jar java_cup.Main -symbols BLOGTokenConstants -parser BLOGParser BLOGParser.cup
cd ../../../