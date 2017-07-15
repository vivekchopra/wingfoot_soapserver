##
# This script will compile all the test files and jar it in into unittest.jar
# 
##
SRC_DIR=~/wingfoot_development

CLASSPATH=${SRC_DIR}/soapserver/unittest/src:${SRC_DIR}/soapserver/build/lib/wsoapServer_90.jar:.:${SRC_DIR}/extern/j2se_kxml.jar 

echo $CLASSPATH

#find . -name '*.java' -exec javac -d ${SRC_DIR}/soapserver/unittest/build -classpath ${CLASSPATH} {} \; -print

find . -name 'RunInterop*.java' -exec javac -d ${SRC_DIR}/soapserver/unittest/build -classpath ${CLASSPATH} {} \; -print

cd ${SRC_DIR}/soapserver/unittest/build

jar -cvf unittest.jar com/*

cp unittest.jar /usr/local/jakarta-tomcat-4.0.3/webapps/wingfoot/WEB-INF/lib

cd ${SRC_DIR}/soapserver/unittest

