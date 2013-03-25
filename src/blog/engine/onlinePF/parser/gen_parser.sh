#!/bin/bash
java -cp ../../../../../lib/JFlex-1.4.3.jar JFlex.Main PolicyLexer.flex
java -cp ../../../../../lib/java_cup.jar java_cup.Main -symbols PolicyTokenConstants -parser PolicyParser PolicyParser.cup
