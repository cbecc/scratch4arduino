#!/bin/sh
javac -classpath RXTXcomm.jar A4S.java processing/src/Firmata.java
mkdir org/firmata
copy processing/src/*.class org/firmata/
jar -cfm A4S.jar manifest.mf *.class org/firmata/*.class
del *.class org/firmata/*.class
