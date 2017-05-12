set classpath="%CD%\bin;%CD%\java-cup-11b.jar"
set main=pl0.PL0_LALR
set compiler_switches=
cd test-pgm
set my_out="%CD%\my_results"
set my_err="%CD%\my_errors"
mkdir %my_out%
mkdir %my_err%
FOR %%i IN ("%CD%\*.pl0") DO java -classpath %classpath% %main% %compiler_switches% %%~nxi 1>%my_out%\my-r-%%~nxi 2>%my_err%\my-e-%%~nxi
pause