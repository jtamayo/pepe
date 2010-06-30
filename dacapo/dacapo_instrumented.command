#!/bin/bash

java -Xmx1024m -XX:MaxPermSize=128m -Xverify:all -javaagent:/Users/juanmtamayo/Projects/pepe/pepe/pepeagent.jar -Xbootclasspath/p:/Users/juanmtamayo/Projects/pepe/pepe/jre/rt_instrumented.jar:/Users/juanmtamayo/Projects/pepe/pepe/bin -jar dacapo-9.12-bach.jar tradebeans

