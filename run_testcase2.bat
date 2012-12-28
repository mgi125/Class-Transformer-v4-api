@echo off
java -server -Xmx4096m -cp bin;data/lib/asm-4.1.jar; tests.APITest2 test.jar
pause