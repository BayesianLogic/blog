mkdir bin

dir /s /B src\*.java > javafiles

javac -verbose -cp lib\gson-2.2.4.jar;lib\java-cup-11b.jar;lib\jflex-1.5.1.jar;lib\commons-math3-3.0.jar;lib\commons-math-2.2.jar;lib\junit-4.10.jar;lib\Jama.jar;bin\ -d bin\ @javafiles

del javafiles