# A simple shell script to wrap the java command.  
# This way, the user does not have to remember the java command
# ** We should expand this and have it as part of the server offering
# As of right now, this is a simple shell script, we can make it interactive

#java -classpath $CLASSPATH:../build/j2se_classes com.wingfoot.soap.server.DeploymentAdmin $1 $2 $3

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver deploy ./unittest.xml

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver deploy ./interopservices.xml

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver deploy ./unittestB.xml

java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver list 

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver undeploy http://soapinterop.org/

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar:./build com.wingfoot.unittest.TestClient

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar:./build com.wingfoot.interop.RunInterop base

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar:./build com.wingfoot.interop.RunInteropB 'GroupB'

#This command will run the unittestB Client
#Please pass it the test you want to run
#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar:./build com.wingfoot.unittestB.TestClient $1
