@ECHO OFF
javac -cp src;test;lib/junit-4.12.jar test/%1.java
IF %ERRORLEVEL% EQU 0 ^
java -cp src;test;lib/junit-4.12.jar;lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore %1