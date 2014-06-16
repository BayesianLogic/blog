
java -cp lib\jflex-1.5.1.jar JFlex.Main src\blog\parse\BLOGLexer.flex
cd src\blog\parse
java -cp ..\..\..\lib\java-cup-11b.jar java_cup.Main -locations -symbols BLOGTokenConstants -parser BLOGParser BLOGParser.cup
cd ..\..\..\
