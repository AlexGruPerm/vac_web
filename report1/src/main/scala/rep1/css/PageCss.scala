package rep1.css

object PageCss {
  val css: String =
    """
      |<style type="text/css">
      |<!--
      |body {
      |	color:#000000;
      |	margin:0;
      |}
      |.label {
      |    width: 180px;
      |    height: 50px;
      |    border-radius: 4px;
      |    text-align: center;
      |    cursor: pointer;
      |    display: block;
      |    font: 14px/50px Tahoma;
      |    transition: all 0.18s ease-in-out;
      |    border: 1px solid #333;
      |    color: #333;
      |}
      |
      |.sel-button-container {
      |    border: 3px solid #fff;
      |    padding: 20px;
      |}
      |
      | select {
      |    width: 250px; /* Ширина списка в пикселах */
      |   }
      |
      |.select_label {
      |    width: 120px;
      |    height: 35px;
      |    border-radius: 3px;
      |    text-align: center;
      |    cursor: pointer;
      |    display: block;
      |    font: 14px/50px Tahoma;
      |    border: 1px solid #333;
      |    color: #333;
      |    float: left;
      |}
      |
      |.test_container { border:1px solid #ccc; width:100%; height: 800px; overflow-y: scroll; top: 0;  left: 0;}
      |
      |.test_undefined{color: black;}
      |.test_fail{color: #E6ACAC;}
      |.test_success{color: #A7D9A3;}
      |.test_executing{color: grey;}
      |
      |.error_msg{color: red; font-size: 16px;}
      |
      |.pre_json{
      |    height: auto;
      |    max-height: 740px;
      |    overflow: auto;
      |    word-break: normal !important;
      |    word-wrap: normal !important;
      |    white-space: pre !important;
      |}
      |
      |span:hover {
      |    background-color: #DCDCDC;
      |}
      |
      |.test_state_undef {background-color: #D3D3D3;}
      |.test_state_executing {background-color: #F9E79F;}
      |.test_state_success {background-color: #8FBC8F;}
      |.test_state_failure {background-color: #E6ACAC;}
      |
      |-->
      |</style>
      |""".stripMargin

}
