#!/bin/sh
#use this script to package parvus

RETVAL=0

##
# variable declarations
##
TMP=/tmp/wingfoot
SOAPSERVER=${WINGFOOT_HOME}/wingfoot_soapserver
WSDL=${WINGFOOT_HOME}/wingfoot_wsdl
SERVERNAME=wsoapServer.jar
DEPLOYMENTNAME=deploymentAdmin.jar
USERGUIDE=Parvus_User_Guide1-0a.doc
PARVUS=parvus.jar
PARVUSWAR=parvus.war
PARVUSNAME=parvus
ZIPNAME=parvus1_0a
WEBXML=web.xml
WFPROPERTIES=wingfoot.properties
README=Readme.txt
LICENSE=LICENSE

#set the WINGFOOT_HOME variable - so this can be used in other scripts
if test -z ${WINGFOOT_HOME}
    then
      echo "Please set your WINGFOOT_HOME variable"
      echo "For BASH, the command is: export WINGFOOT_HOME=PATH/TO/WINGFOOT/DEVEL"
      exit ${RETVAL}
fi

if test -z $1
    then
      echo "usage: package.sh directory to package to"
      exit ${RETVAL}
fi

echo "Packaging Parvus"
mkdir $1/parvus
mkdir $1/parvus/lib
mkdir $1/parvus/doc
mkdir $1/parvus/conf

cp ${SOAPSERVER}/doc/${README} $1/parvus/
cp ${SOAPSERVER}/doc/${LICENSE} $1/parvus/
cp ${SOAPSERVER}/doc/${USERGUIDE} $1/parvus/doc/
cp ${SOAPSERVER}/conf/${WEBXML} $1/parvus/conf/
cp ${SOAPSERVER}/conf/${WFPROPERTIES} $1/parvus/conf

##now the fun part
if test ! -d ${TMP}
    then
    mkdir ${TMP}
else
    rm -r ${TMP}/*
fi

cp ${SOAPSERVER}/build/lib/${SERVERNAME} ${TMP}
cp ${SOAPSERVER}/build/lib/${DEPLOYMENTNAME} ${TMP}
cp ${WSDL}/build/lib/${PARVUS} ${TMP}

cd ${TMP}
jar -xf ${SERVERNAME}
jar -xf ${PARVUS} 
jar -xf ${DEPLOYMENTNAME}
rm -rf ${PARVUS}
rm -rf ${SERVERNAME}
rm -rf ${DEPLOYMENTNAME}
rm -rf META-INF
jar -cf ${PARVUS} org/* com/*

cp ${PARVUS} $1/parvus/lib

#build the war file
mkdir ${PARVUSNAME}
mkdir ${PARVUSNAME}/WEB-INF
mkdir ${PARVUSNAME}/WEB-INF/lib
mkdir ${PARVUSNAME}/WEB-INF/classes
cp ${PARVUS} ${PARVUSNAME}/WEB-INF/lib/
cp ${SOAPSERVER}/conf/${WEBXML} ${PARVUSNAME}/WEB-INF/
cp ${SOAPSERVER}/conf/${WFPROPERTIES} ${PARVUSNAME}/WEB-INF/classes
cd ${TMP}/${PARVUSNAME}
jar -cf ${PARVUSWAR} * 

mv ${PARVUSWAR} $1/parvus/lib

#rm -rf ${TMP}

cd $1

zip -r ${ZIPNAME} parvus/
