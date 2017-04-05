
cd my-results

for i in *
do
    vimdiff $i ../results/$i
done
