#!/bin/bash

jar xvf dacapo-9.12-bach.jar dat/daytrader.zip
mkdir -p dat/geronimo-jetty6-minimal-2.1.4/repository/com/h2database/h2/1.2.121/
cp ../h2database-read-only/h2/bin/h2.jar dat/geronimo-jetty6-minimal-2.1.4/repository/com/h2database/h2/1.2.121/h2-1.2.121.jar
jar uvf dat/daytrader.zip -C dat geronimo-jetty6-minimal-2.1.4/repository/com/h2database/h2/1.2.121/h2-1.2.121.jar
jar uvf dacapo-9.12-bach.jar dat/daytrader.zip