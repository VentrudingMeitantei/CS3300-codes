#!/bin/bash

IN_FOLDER=test_in
javac P6.java
for FILE in $IN_FOLDER/*.miniRA
do
    FILENAME=${FILE%.miniRA}
    FILENAME=${FILENAME#*/}
    echo $FILENAME
    java P6 < $IN_FOLDER/$FILENAME.miniRA > test_out/$FILENAME.s
    mkdir tmp
    cp $IN_FOLDER/$FILENAME.miniRA tmp
    cp test_out/$FILENAME.s tmp
    cp Mars4_5.jar tmp
    cp kgi.jar tmp
    cd tmp
    java -jar kgi.jar < $FILENAME.miniRA > miniout
    java -jar Mars4_5.jar $FILENAME.s > mipsout
    sed -i '1,2d' mipsout 
    sed -i '$d' mipsout 
    diff mipsout miniout
    cd ..
    rm -rf tmp
done
