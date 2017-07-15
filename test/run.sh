# A simple shell script to wrap the java command.  
# This way, the user does not have to remember the java command
# ** We should expand this and have it as part of the server offering
# As of right now, this is a simple shell script, we can make it interactive

#java -classpath $CLASSPATH:../build/j2se_classes com.wingfoot.soap.server.DeploymentAdmin $1 $2 $3

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver deploy ./hello.xml

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver list 

#java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j4se_kxml.jar com.wingfoot.soap.server.DeploymentAdmin http://localhost:8080/wingfoot/servlet/wserver undeploy http://www.wingfoot.com

java -cp ../build/lib/wsoapServer_90.jar:.:../../extern/j2se_kxml.jar Test
