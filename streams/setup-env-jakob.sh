# Before using StreamIt:
# - set and export STREAMIT_HOME
# - set JDK_1_6_DIR
# - source this script

### Location of unpacked StreamIt tree
if [[ -z "$STREAMIT_HOME" || -z "$JDK_1_6_DIR" ]]; then
    echo "Please set the following environment variables: STREAMIT_HOME, JDK_1_6_DIR"
    exit 1
fi

### Location of ANTLR jar file (test: 'java antlr.tool')
ANTLRJAR=$STREAMIT_HOME/uns/antlr.jar

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
PATH=$JDK_1_6_DIR/bin:${STREAMIT_HOME}:${PATH}

### Declare host type
STRC_HOST_TYPE="x86-linux"

export STREAMIT_HOME CLASSPATH PATH STRC_HOST_TYPE
