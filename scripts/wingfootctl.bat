@echo off

if "%WINGFOOT_HOME%" == "" goto noWFHome

set CLASSPATH=%WINGFOOT_HOME%\lib\deploymentAdmin.jar
set DEPLOYMENTADMIN=com.wingfoot.soap.server.DeploymentAdmin

if ""%1"" == ""deploy"" goto doDeploy
if ""%1"" == ""undeploy"" goto doUndeploy
if ""%1"" == ""list"" goto doList

echo Usage: wingfootctl.bat [deploy undeploy list] [url] [DescriptorFileName Service to remove]
goto end

:doDeploy
echo Deploying...
java -classpath %CLASSPATH% %DEPLOYMENTADMIN% %2 deploy %3
goto end

:doUndeploy
echo Undeploying...
java -classpath %CLASSPATH% %DEPLOYMENTADMIN% %2 undeploy %3
goto end

:doList
echo Listing...
java -classpath %CLASSPATH% %DEPLOYMENTADMIN% %2 list 

goto end

:noWFHome
echo Please set the WINGFOOT_HOME variable
echo You can set it by entering: set WINGFOOT_HOME=C:\path\to\wingfoot
goto end

:end
