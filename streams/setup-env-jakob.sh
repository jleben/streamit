### Location of unpacked StreamIt tree
STREAMIT_HOME=$HOME/code/streamit/streams

### Location of ANTLR jar file (test: 'java antlr.tool')
#ANTLRJAR=/home/jakob/apps/antlr/antlr-4.7.1-complete.jar
ANTLRJAR=/home/jakob/code/streamit/streams/uns/antlr.jar

### Update CLASSPATH
CLASSPATH=.:${CLASSPATH}
CLASSPATH=${CLASSPATH}:${ANTLRJAR}
CLASSPATH=${CLASSPATH}:${STREAMIT_HOME}/src
CLASSPATH=${CLASSPATH}:${STREAMIT_HOME}/3rdparty
CLASSPATH=${CLASSPATH}:${STREAMIT_HOME}/3rdparty/cplex/cplex.jar
CLASSPATH=${CLASSPATH}:${STREAMIT_HOME}/3rdparty/jgraph/jgraph.jar
CLASSPATH=${CLASSPATH}:${STREAMIT_HOME}/3rdparty/JFlex/jflex.jar
CLASSPATH=${CLASSPATH}:${STREAMIT_HOME}/3rdparty/jcc/jcc.jar

### Update the shell path
PATH=/home/jakob/apps/jdk1.6.0_45/bin:${STREAMIT_HOME}:${PATH}

### Declare host type
STRC_HOST_TYPE="x86-linux"

export STREAMIT_HOME CLASSPATH PATH STRC_HOST_TYPE
