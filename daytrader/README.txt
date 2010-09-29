Warnings:

A. Set JAVA_HOME and MAVEN_OPTS before running ant

    1. Set your JAVA_HOME environment variable appropriately:
        On Mac OS X:
            export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home            
        On Ubuntu 9.04:
            export JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun
            
    2. Set ant and maven environment variables if necessary.  In particular,
       for some jvms it is necessary to explicitly request a larger heap size.
       It is necessary to set the maven options because the trade benchmarks
       are built by maven (called by ant).  As another example, you may wish
       for ant to use a proxy when downloading (there is a lot to be
       downloaded).   Some examples:
            export ANT_OPTS="-Xms512M -Xmx512M"
            export MAVEN_OPTS="-Xms512M -Xmx512M"
       or
            export ANT_OPTS="-Dhttp.proxyHost=xxx.xxx.xxx.xxx -Dhttp.proxyPort=3128"