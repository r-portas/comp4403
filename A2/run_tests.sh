cd test-pgm
for i in test*.pl0
do
	./mkresult.sh $i
	echo '------------------------------------------'
done
