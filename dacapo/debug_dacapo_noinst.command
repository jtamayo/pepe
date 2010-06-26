#!/bin/bash

java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -Xverify:all -jar dacapo-9.12-bach.jar tradebeans

