mkdir bin

dir /s /B src/*.java > javafiles

javac -verbose -cp lib/java_cup.jar:lib/JFlex-1.4.3.jar:lib/commons-math3-3.0.jar:lib/commons-math-2.2.jar:lib/junit-4.10.jar:lib/Jama.jar:bin/ -d bin/ @javafiles

delete javafiles