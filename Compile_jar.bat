"D:\Program Files (x86)\Java\jdk1.7.0_45\bin\javac" -source 1.4 -classpath RXTXcomm.jar A4S.java processing/src/Firmata.java
pause
mkdir org\firmata
pause
copy processing\src\*.class org\firmata\
pause
"D:\Program Files (x86)\Java\jdk1.7.0_45\bin\jar" -cfm A4S.jar manifest.mf *.class org\firmata\*.class
del *.class org\firmata\*.class

pause

