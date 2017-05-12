CLASSPATH=../bin:../java-cup-11b.jar
#MAIN=pl0.PL0_RD
MAIN=pl0.PL0_LALR
export CLASSPATH

if [ $# -lt 1 ]; then
    echo "Usage: mkresult test_file"
elif [ -f "$1" ]; then
#   java -classpath $CLASSPATH ${MAIN} $1 -o run.ibsm 2> errors/e-$1 | tee results/r-$1
#    java ${MAIN} $1 2> errors/e-$1 | tee results/r-$1
#   java ${MAIN} $1 -v 2> errors/e-$1 | tee results/r-$1
#   java ${MAIN} $1 -v -t 2> errors/e-$1 | tee results/r-$1
   java ${MAIN} $1 2> errors/e-$1 | tee results/r-$1
    cat errors/e-$1
else
    echo "<$1>" does not exist
fi
