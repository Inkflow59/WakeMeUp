@echo off
echo Synchronisation du projet Android...
cd /d "C:\Users\Adiss-06\Documents\GitHub\WakeMeUp"
gradlew.bat clean
gradlew.bat build
echo Synchronisation terminée!
pause

