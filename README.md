1) Project location: E:\PROJECTS\vac_web
2) compile:
   cd /d E:\PROJECTS\vac_web
   sbt clean compile
3) package:
   sbt universal:packageBin
   It produce new zip file:
   E:\PROJECTS\vac_web\webui\target\universal\webui-0.0.1.zip (69 Mb)
4) run
   unzip webui-0.0.1.zip as cd E:\PROJECTS\vac_web\webui\target\universal\webui-0.0.1\webui-0.0.1
   >bin\webui.bat
   output:
   Слишком длинная входная строка.
   Ошибка в синтаксисе команды.


