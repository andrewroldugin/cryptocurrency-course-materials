@ECHO OFF
javac -cp src Main.java
IF %ERRORLEVEL% EQU 0 java -cp .;src Main