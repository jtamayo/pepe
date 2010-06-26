#!/bin/bash

java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -Xverify:all -javaagent:/Users/juanmtamayo/Projects/pepe/pepe/pepeagent.jar -Xbootclasspath/p:/Users/juanmtamayo/Projects/pepe/pepe/jre/rt_instrumented.jar:/Users/juanmtamayo/Projects/pepe/pepe/bin -jar dacapo-9.12-bach.jar tradebeans

