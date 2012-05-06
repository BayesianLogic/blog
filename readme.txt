1. Working with Eclipse (recommended) 
eclipse --> new projet --> point to directory (use the same name as project name)
--> set src as source directory, set bin as build directory --> import all jar's in 
lib/ directory 
Everything would be automatic from then on.  

2. manually compile (but won't regenerate Lexer and Parser from cup, see below)
./compile.sh

3. to generate Lexer and Parser 
!!!no need to do this step unless you touched BLOGLexer.flex and/or BLOGParser.cup
./gen_parser.sh

4. run blog
./run.sh example/simple-aircraft.blog

$date May 5, 2012$
