#!/bin/sh 
# use this script to package WSOAP product

RETVAL=0

if test -z $1
    then
      echo "usage: package.sh directory to package to"
      exit ${RETVAL}
fi

echo "Packaging Wingfoot Soap Server in $1 directory"

mkdir $1/wingfoot
mkdir $1/wingfoot/lib
mkdir $1/wingfoot/conf
mkdir $1/wingfoot/doc
mkdir $1/wingfoot/example
mkdir $1/wingfoot/scripts
cp ../build/lib/*.jar $1/wingfoot/lib
cp ../scripts/install $1/wingfoot/scripts/install
cp ../scripts/wingfootctl $1/wingfoot/scripts/wingfootctl
cp ../doc/README $1/wingfoot/
cp ../webpages/index.html $1/wingfoot/doc/index.html
cp ../conf/interopservices.xml $1/wingfoot/scripts/interopservices.xml
cp ../conf/web.xml $1/wingfoot/conf/web.xml
cp ../conf/wingfoot.properties $1/wingfoot/conf/wingfoot.properties
cp ../test/unittest/build/example.jar $1/wingfoot/lib
tar -cvf wingfoot.tar wingfoot

exit ${RETVAL}
