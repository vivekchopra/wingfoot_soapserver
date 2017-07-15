#javac -d ../../../../build -classpath ../../../../../build/lib/wsoapServer_90.jar:.:../../../../../../extern/j2se_kxml.jar *.java
javac -d ../../../../build -classpath ../../../../../build/lib/wsoapServer_90.jar:.:../../../../../../extern/j2se_kxml.jar:/home/baldwinl/devel/wingfoot_development/soap/test *.java

cd ../../../../build

jar -cvf runinterop.jar com/*

mv runinterop.jar /usr/local/tomcat/webapps/wingfoot/WEB-INF/lib

cd ../src/com/wingfoot/interop


