#! /bin/bash
#
# A script for building and deploying the server
# 


RETVAL=0

#set the WINGFOOT_HOME variable - so this can be used in other scripts
if test -z ${WINGFOOT_HOME}
    then
      echo "Please set your WINGFOOT_HOME variable"
      echo "For BASH, the command is: export WINGFOOT_HOME=PATH/TO/WINGFOOT/DEVEL"
      exit ${RETVAL}
fi

#some declarations
PARVUS=parvus.jar
WSDL=${WINGFOOT_HOME}/wingfoot_wsdl
TMP=/tmp/wingfoot
SOAPSERVER=${WINGFOOT_HOME}/wingfoot_soapserver
COMPILED_CLASSES=${SOAPSERVER}/build/j2se_classes
SERVERNAME=wsoapServer.jar
ADMINNAME=deploymentAdmin.jar
SOURCE_CONF_FILE=${SOAPSERVER}/conf/wingfoot.properties
TARGET_CONF_FILE=${SOAPSERVER}/build/wingfoot/WEB-INF/classes/wingfoot.properties
CLASSPATH=${WINGFOOT_HOME}/wingfoot_parser/build/lib/j2se_kxml.jar:${WINGFOOT_HOME}/wingfoot_extern/java/servlet.jar:${SOAPSERVER}/src:${WINGFOOT_HOME}/wingfoot_extern/retroguard-v1.1/retroguard.jar:${COMPILED_CLASSES}:${WINGFOOT_HOME}/wingfoot_wsdl/build/lib/parvus.jar



compile() {
   echo "Compiling Java Classes"
    for javafile in `find ${SOAPSERVER}/src -type f -name '*.java' -print`
      do
      echo $javafile
      javac -g:none -classpath $CLASSPATH -d $COMPILED_CLASSES $javafile
    done
}

obfuscate() {
   echo "Obfuscating Java Classes"
   cd $COMPILED_CLASSES
   jar -cf serverclasses.jar ${COMPILED_CLASSES}/com/*
   java -classpath ${CLASSPATH} RetroGuard serverclasses.jar ${SERVERNAME} ${SOAPSERVER}/build/scriptj2se.rgs
   mv ${SERVERNAME} ${SOAPSERVER}/build/lib/
   cd ${SOAPSERVER}/build/lib
   echo "Copying xml parser"
   cp ${WINGFOOT_HOME}/wingfoot_parser/build/lib/j2se_kxml.jar ${SOAPSERVER}/build/lib/j2se_kxml.jar
   jar -xf ${SERVERNAME}
   jar -xf j2se_kxml.jar
   rm -rf META-INF
   rm ./*.jar
   echo "Creating admin jar file"
   jar -cf ${ADMINNAME} org/* com/wingfoot/soap/server/DeploymentAdmin.class  com/wingfoot/soap/server/DeploymentException.class com/wingfoot/soap/server/Service.class
   echo "Creating master jar file"
   jar -cf ${SERVERNAME} org/* com/wingfoot/soap/*.class com/wingfoot/soap/encoding/*.class com/wingfoot/soap/transport/*.class com/wingfoot/soap/server/HTTPListener.class com/wingfoot/soap/server/RouterException.class com/wingfoot/soap/server/Service.class com/wingfoot/soap/server/SOAPRouter.class
   rm -rf com
   rm -rf org
   cd ${SOAPSERVER}
}

example() {
SRC_DIR=$SOAPSERVER/test/unittest
EXAMPLE_COMPILED_CLASSES=$SOAPSERVER/test/unittest/build
find ${SRC_DIR}/src/com/wingfoot/interop -name '*.java' -exec javac -d ${EXAMPLE_COMPILED_CLASSES} -classpath ${SOAPSERVER}/build/lib/${SERVERNAME}:${SRC_DIR}/src {} \; -print
cd ${EXAMPLE_COMPILED_CLASSES}
jar -cf example.jar com/*
cd ${SOAPSERVER}
}

clean() {
   echo "Deleting files."
   find $COMPILED_CLASSES -type f -name '*.class' |  xargs rm -f 
   rm -f ${SOAPSERVER}/build/wingfoot.war 
   rm -f ${SOAPSERVER}/build/lib/*.jar 
   rm -rf ${SOAPSERVER}/build/wingfoot
}

war() {

   echo "Enter the location of the deployment file"
   read deploymentFile
   echo "Enter the location of the logger"
   read logger


   echo "Generating WAR file"
   if test -z "$deploymentFile" || test -z "$logger"
   then
     echo "No directory locations were passed"
     exit ${RETVAL}
   fi

    if test ! -d ${SOAPSERVER}/build/wingfoot 
	then
	  mkdir ${SOAPSERVER}/build/wingfoot
	  mkdir ${SOAPSERVER}/build/wingfoot/WEB-INF
	  mkdir ${SOAPSERVER}/build/wingfoot/WEB-INF/lib
	  mkdir ${SOAPSERVER}/build/wingfoot/WEB-INF/classes
	  mkdir ${SOAPSERVER}/build/wingfoot/images
    else
	  rm -r ${SOAPSERVER}/build/wingfoot/WEB-INF/classes/*
    fi 
	
    
    echo "Copying wingfoot.properties file"


    dLocation=$(echo $deploymentFile | sed -e 's/\//\\\//g') #massage the entered data
    logLocation=$(echo $logger | sed 's/\//\\\//g') #massage the entered data
 

    sed  -e 's/$DEPLOYMENT_FILE_LOCATION/'$dLocation'/' -e 's/$LOGGER_LOCATION/'$logLocation'/'  ${SOURCE_CONF_FILE} >> ${TARGET_CONF_FILE}

    echo "Copying web.xml file"
    cp ${SOAPSERVER}/conf/web.xml ${SOAPSERVER}/build/wingfoot/WEB-INF/web.xml
    
    echo "Copying ${SERVERNAME}"
    
    ##
    # combines the wsoapServer.jar and parvus.jar files together
    ##
    
    if test ! -d ${TMP}
	then
	  mkdir ${TMP}
    else
	rm -r ${TMP}/*
    fi

    cp ${SOAPSERVER}/build/lib/${SERVERNAME} ${TMP}
    cp ${WSDL}/build/lib/parvus.jar ${TMP}
    
    cd /tmp/wingfoot
    jar -xf ${SERVERNAME}
    jar -xf ${PARVUS} 
    rm -rf ${PARVUS}
    rm -rf ${SERVERNAME}
    rm -rf META-INF
    jar -cf ${PARVUS} org/* com/*
    
    #cp ${SOAPSERVER}/build/lib/${SERVERNAME} ${SOAPSERVER}/build/wingfoot/WEB-INF/lib/${SERVERNAME}
    cp ${PARVUS} ${SOAPSERVER}/build/wingfoot/WEB-INF/lib/${PARVUS}

    #create the war
    cd ${SOAPSERVER}
    cp ${SOAPSERVER}/webpages/*.html ${SOAPSERVER}/build/wingfoot
    cd ${SOAPSERVER}/build/wingfoot
    jar -cf wingfoot.war .
    mv wingfoot.war ${SOAPSERVER}/build
    echo "******************"
    echo "The wingfoot.war file has been created."
    echo "It is located in: ${SOAPSERVER}/build/wingfoot.war"
    echo "******************"
}

case $1 in
    compile)
	compile
	;;
    
    obfuscate)
	obfuscate
	;;
    
    clean)
	clean
	;;
    
    war)
	war
	;;
    example)
	example
	;;
    all)
	clean
	compile
	obfuscate 
	war
	;;
    help)
        echo "usage j2secompile.sh: [compile|obfuscate|clean|war|all|help]"
        echo "compile   - compiles the server sources"
        echo "obfuscate - obfuscates the compiled server sources"
        echo "clean     - clean"
	echo "example   - compile the example"
        echo "war	- packages server source and configuration files into a war file"
        echo "all	- does all of the above"
        echo "help 	- so help you god"
	;;
    
    *)
    
    echo "usage j2secompile.sh: [compile|obfuscate|clean|example|war|all|help]"
    ;;

esac

exit $RETVAL
