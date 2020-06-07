@echo off

::----------------------------------------------------------------------
:: Satin Startup Script
::----------------------------------------------------------------------

SET CLASS_PATH=lib\*
SET CLASS_PATH=%CLASS_PATH%;etc

java -cp %CLASS_PATH% --enable-preview alankstewart.satin.Satin

