find . -name '*.java' -exec javac -d ../../../../build -classpath ../../../../../build/lib/wsoapServer_90.jar:.:../../../../../../extern/j2se_kxml.jar:/home/baldwinl/devel/wingfoot_development/soap/test {} \; -print

cd ../../../../build

jar -cvf unittest.jar com/*

mv unittest.jar /usr/local/tomcat/webapps/wingfoot/WEB-INF/lib

cd ../src/com/wingfoot/unittest

