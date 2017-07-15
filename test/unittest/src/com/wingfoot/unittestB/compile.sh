javac -d ../../../../build -classpath ../../../../../build/lib/wsoapServer_90.jar:.:../../../../../../extern/j2se_kxml.jar *.java

cd ../../../../build

jar -cvf unittest.jar com/*

cp unittest.jar /usr/local/tomcat/webapps/wingfoot/WEB-INF/lib

cd ../src/com/wingfoot/unittest

