D=`pwd`
CLASSPATH=$D/java-cup-11b.jar:$D/jflex-1.6.1.jar:$CLASSPATH

cd src/parser
java -cp $CLASSPATH java_cup.Main -interface -locations \
	-parser CUPParser -symbols CUPToken \
	PL0.cup
cd ../..

java -cp $CLASSPATH jflex.Main src/parser/PL0.flex

javac -cp $CLASSPATH -g -d bin -sourcepath src src/pl0/PL0_LALR.java
