package rep1.html

import rep1.css.PageCss
import rep1.js.PageJs

object PageHtml {
  def pageHtml(dictsCombos: String): String =
    s"""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
       |<html xmlns="http://www.w3.org/1999/xhtml">
       |<head>
       |    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
       |    <title>Reports</title>
       |${PageJs.js}
       |${PageCss.css}
       |</head>
       |<body bgcolor="#f7f7f7">
       |<table width="100%" border="2">
       |    <tr>
       |        <td colspan="100%" bgcolor="#D3D3D3" height="40px">
       |          <table border="1" width="100%">
       |           <tr>
       |            <td width="25%"><div id="div_summary">Тех информация:</div></td>
       |            <td width="25%">Центр</td>
       |            <td width="25%">
       |            <div id="svod">СВОДНАЯ.......</div>
       |            </td>
       |            <td width="25%">
       |            <div id="export_report_1">
       |              <div id="btn_report_export" onclick="btnShowReport(2)" class="select_label">ЭКСПОРТ</div>
       |              <p id="export_prcnt" hidden >0 %</p><p id="export_pb" hidden>..........</p>
       |              </br>
       |              <p id="p_for_link" hidden>Данные подготовлены, запись в Excel файл (3-5 сек.)</p>
       |              <a href="http://localhost:8081/static/13_05_2024___12_41_47.xlsx" hidden id="export_link">скачать файл Excel с выгрузкой</a>
       |            </div>
       |            </td>
       |           </tr>
       |          </table>
       |        </td>
       |    </tr>
       |    <tr>
       |        <td width="20%" height="700px" >
       |           Номер страницы:
       |           <input type="text" maxlength="6" id="pagenum" name="pagenum" value="1"></br>
       |           Число строк на странице:
       |           <input type="text" maxlength="6" id="pagerowscnt" name="pagerowscnt" value="100">
       |           </br></br>
       |        $dictsCombos
       |        </br>
       |        <div id="btn_show_report" onclick="btnShowReport(1)" class="select_label">Показать отчет</div>
       |        </td>
       |        <td>
       |          <div id="report_rows" class="test_container">
       |            <table id="report_table" border=2px solid; border-collapse: collapse; bgcolor="#f9f9f9">
       |            </table>
       |          </div>
       |        </td>
       |    </tr>
       |    <tr>
       |        <td colspan="100%" bgcolor="#D3D3D3" height="40px">
       |          Тут будет пагинация
       |        </td>
       |    </tr>
       |</table>
       |</body>
       |</html>
       |""".stripMargin
}
