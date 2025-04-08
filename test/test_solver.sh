cd ../src
java KlotskiSolver.java > out.txt &
pid=$!
sleep 1
kill -9 $pid
