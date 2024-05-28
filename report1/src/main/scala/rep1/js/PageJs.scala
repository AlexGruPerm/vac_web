package rep1.js

object PageJs {

  val pushButtonReport1: String =
    """
      | window.onload = function() {
      |    console.log('window.onload Event');
      |    document.getElementById("btn_show_report1").click();
      |};
      |""".stripMargin

  val js: String =
    """
      |<script>
      |        var exportID;
      |        var exportLinkHref;
      |        var responseStatus;
      |        var responseText;
      |        var checkedTestId;
      |        var checkedCommon;
      |        var checkedTestIdJson;
      |        var isExportExecuting = 0;
      |        var intervalPb;
      |        var reportJsonParams;
      |        var omsu_id   = 0;
      |
      |        var period_id = 0;
      |
      |        var r2_omsu_name;
      |        var r2_org_name;
      |        var r2_year;
      |        var r2_pd_total;
      |        var r2_pd_error;
      |        var r2_pd_prcnt_error;
      |
      |        var tdValColor = "";
      |
      |        var cWhite      = "#FFFFFF";
      |        var cGreen      = "#2ECC71";
      |        var cLightGreen = "#ABEBC6";
      |        var cLightRed   = "#F5B7B1";
      |        var cRed        = "#E74C3C";
      |
      | window.onload = function() {
      |    console.log('window.onload Event');
      |
      |    r2_omsu_name      = document.getElementById("r2_omsu_name");
      |    r2_org_name       = document.getElementById("r2_org_name");
      |    r2_year           = document.getElementById("r2_year");
      |    r2_pd_total       = document.getElementById("r2_pd_total");
      |    r2_pd_error       = document.getElementById("r2_pd_error");
      |    r2_pd_prcnt_error = document.getElementById("r2_pd_prcnt_error");
      |
      |};
      |
      |        progReport1ButtonPush
      |
      |        function myStopFunction() {
      |          console.log("STOP checking progress..............");
      |          clearInterval(intervalPb);
      |        }
      |
      |        function checkSuccessTest(testId){
      |          const divTests = document.querySelector(`div[id="test_list"]`);
      |          const checkBox = divTests.querySelectorAll(`span[id="test_${testId}"]`)[0];
      |          checkBox.classList.value = "";
      |          checkBox.className = 'test_state_success';
      |        }
      |
      |        function setSpansUndefined(){
      |          const divTests = document.querySelector(`div[id="test_list"]`);
      |          const checkBox = divTests.querySelectorAll(`span[id^=test]`);
      |          Array.from(checkBox).map(elm => elm.classList.value = "");
      |          Array.from(checkBox).map(elm => elm.className = "test_state_undef");
      |        }
      |
      |        function checkFailTest(testId){
      |          const divTests = document.querySelector(`div[id="test_list"]`);
      |          const checkBox = divTests.querySelectorAll(`span[id="test_${testId}"]`)[0];
      |          checkBox.classList.value = "";
      |          checkBox.className = 'test_state_failure';
      |        }
      |
      |        async function getTestsState() {
      |             console.log('-- request tests state ---------------------------------------------- SID = ',globalSessionId);
      |             const curr_state = document.getElementById("test_current_state");
      |               const response = await fetch(`check/`+globalSessionId, {method: "GET"});
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('-- error ----------------------------------------------');
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     curr_state.className = 'error_msg';
      |                     curr_state.innerHTML = `ERROR:<br/> ${JSON.parse(responseText).message}`;
      |                     console.log('Response text = ',responseText);
      |                     console.log('---------------------------------------------------------');
      |                   } else {
      |                     curr_state.classList.remove('error_msg');
      |                     curr_state.innerHTML = "";
      |                     responseText = await response.text();
      |
      |                     const response_json = JSON.parse(responseText);
      |                     const t_total    = response_json.tests.total;
      |                     const t_enabled  = response_json.tests.enabled;
      |                     const t_disabled = response_json.tests.disabled;
      |                     const t_executed = response_json.tests.executed;
      |                     const t_success  = response_json.tests.success;
      |                     const t_failure  = response_json.tests.failure;
      |                     const t_percent  = Math.round((t_executed*100)/t_enabled);
      |
      |
      |                     curr_state.innerHTML  = `<div>total : ${t_total} </div>`;
      |                     curr_state.innerHTML += `<div>enabled : ${t_enabled} </div>`;
      |                     curr_state.innerHTML += `<div>disabled : ${t_disabled} </div>`;
      |                     curr_state.innerHTML += `<div>executed : ${t_executed} </div>`;
      |                     curr_state.innerHTML += `<div>success : ${t_success} </div>`;
      |                     curr_state.innerHTML += `<div>failure : ${t_failure} </div>`;
      |                     curr_state.innerHTML += `<div>percent : ${t_percent} </div>`;
      |
      |                       console.log("success array - ",response_json.tests.successList);
      |                       response_json.tests.successList.map(
      |                         succElem => checkSuccessTest(succElem)//console.log(succElem)
      |                       );
      |                       response_json.tests.failureList.map(
      |                         succElem => checkFailTest(succElem)//console.log(succElem)
      |                       );
      |
      |                     curr_state.classList.remove('test_state_undef');
      |                       if (t_executed < t_enabled) {
      |                         curr_state.className = 'test_state_executing';
      |                         console.log('set class - test_state_executing ');
      |                       } else if (t_enabled == t_success) {
      |                         console.log('set class - test_state_success ');
      |                         curr_state.className = 'test_state_success';
      |                       } else {
      |                         curr_state.className = 'test_state_failure';
      |                         console.log('set class - test_state_failure ');
      |                       }
      |
      |                   }
      |                   return response;
      |        }
      |
      |
      |        function getTestsAgrInfo() {
      |          if (isTestsExecuting == 1) {
      |          getTestsState();
      |          }
      |        }
      |
      |        async function getTestInfo(testId) {
      |             console.log('-- request test info ---------------------------------------------- ID= ',testId);
      |             const pre_json = document.getElementById("json_test_info");
      |             const resp_message = document.getElementById("resp_message");
      |               const response = await fetch(`test_info/`+globalSessionId+`/`+testId, {method: "GET"});
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('-- error ----------------------------------------------');
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     resp_message.className = 'error_msg';
      |                     resp_message.innerHTML = `ERROR:<br/> ${JSON.parse(responseText).message}`;
      |                     console.log('Response text = ',responseText);
      |                     console.log('---------------------------------------------------------');
      |                   } else {
      |                     resp_message.classList.remove('error_msg');
      |                     resp_message.innerHTML = "";
      |                     responseText = await response.text();
      |                     //pre_json.textContent = JSON.stringify(JSON.parse(responseText), undefined, 2);
      |                     pre_json.innerHTML = responseText;
      |                   }
      |                   return response;
      |        }
      |
      |        async function test_info(test){
      |          const testId = test.id.replace("test_","");
      |          console.log('Info for test = ',testId);
      |          const resultJson = await getTestInfo(testId);
      |        }
      |
      |        function parse_response_populate_tests(tests_json){
      |          globalSessionId = tests_json.session.id;
      |          console.log("SessionId : ",globalSessionId);
      |          const tests_container = document.getElementById("test_list");
      |          tests_container.innerHTML = "";
      |            for (test of tests_json.tests) {
      |                tests_container.innerHTML += `<input type="checkbox" id="test_${test.id}" name="test_${test.id}" ><span id="test_${test.id}" onclick="test_info(this)" class="test_undefined">${test.name}</span><br/>`;
      |            }
      |        }
      |
      |        function loadFile(file){
      |            return new Promise(resolve=>{
      |                let reader = new FileReader()
      |                reader.onload = function(event) {
      |                    let data = event.target.result
      |                    resolve(data)
      |                }
      |                reader.readAsText(file)
      |            })
      |        }
      |
      |        async function loadJsonTests() {
      |             console.log('Begin loading new JSON file with tests into server.');
      |             var file = fileupload.files[0];
      |             var fileContent = await loadFile(file);
      |             console.log('-- request ----------------------------------------------');
      |             console.log(fileContent);
      |             console.log('---------------------------------------------------------');
      |               const response = await fetch(`load_test`, {
      |                    method: "POST",
      |                    headers: {
      |                     'Accept': 'application/json',
      |                     'Content-Type': 'application/json'
      |                    },
      |                    body: fileContent
      |                  });
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('-- error ----------------------------------------------');
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     console.log('Response text = ',responseText);
      |                     console.log('---------------------------------------------------------');
      |                     return response;
      |                   } else {
      |                     return response.json();
      |                   }
      |        }
      |
      |        async function btnUploadTestFile() {
      |             const resultJson = await loadJsonTests();
      |             console.log('-- response ---------------------------------------------');
      |             console.log(resultJson);
      |             console.log('---------------------------------------------------------');
      |             const resp_message = document.getElementById("resp_message");
      |             const tests_container = document.getElementById("test_list");
      |               if (responseStatus == 400) {
      |                 tests_container.innerHTML = "";
      |                 resp_message.className = 'error_msg';
      |                 resp_message.innerHTML = `ERROR:<br/> ${JSON.parse(responseText).message}`;
      |               } else {
      |                 resp_message.classList.remove('error_msg');
      |                 resp_message.innerHTML = "";
      |                 parse_response_populate_tests(resultJson);
      |               }
      |        }
      |
      |        async function startJsonTests() {
      |             console.log('Send id of test to execting.');
      |             console.log('-- request ----------------------------------------------');
      |             console.log(checkedTestIdJson);
      |             console.log('---------------------------------------------------------');
      |             setSpansUndefined();
      |
      |               const response = await fetch(`start_test`, {
      |                    method: "POST",
      |                    headers: {
      |                     'Accept': 'application/json',
      |                     'Content-Type': 'application/json'
      |                    },
      |                    body: checkedTestIdJson
      |                  });
      |               const resp_message = document.getElementById("resp_message");
      |               const tests_container = document.getElementById("test_list");
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('-- error ----------------------------------------------');
      |                     myStopFunction();
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     console.log('Response text = ',responseText);
      |                     resp_message.className = 'error_msg';
      |                     resp_message.innerHTML = `ERROR:<br/> ${JSON.parse(responseText).message}`;
      |                     console.log('---------------------------------------------------------');
      |                     return response;
      |                   } else {
      |                     myStopFunction();
      |                     getTestsState();
      |                     resp_message.classList.remove('error_msg');
      |                     resp_message.innerHTML = "";
      |                     return response.json();
      |                   }
      |        }
      |
      |        async function btnStartTests(){
      |          console.log('-- start tests -------------------- isTestsExecuting=',isTestsExecuting);
      |          const pre_json = document.getElementById("json_test_info");
      |          pre_json.innerHTML = "";
      |          const curr_state = document.getElementById("test_current_state");
      |          curr_state.innerHTML = "";
      |          const tests_container = document.getElementById("test_list");
      |          checkedTestId = Array.from(tests_container.querySelectorAll("input[type=checkbox]:checked"))
      |            .map((elem) => parseInt(elem.id.replace("test_","")));
      |          checkedCommon = "{\"sid\" : \"" + globalSessionId + "\" , \"ids\" : "+JSON.stringify(checkedTestId)+"}";
      |          console.log("checkedCommon = ",checkedCommon);
      |          checkedTestIdJson = JSON.parse(JSON.stringify(checkedCommon));
      |          console.log("checkedTestIdJson = ",checkedTestIdJson);
      |             if (checkedTestId.length) {
      |               console.log("START checking..............");
      |               isTestsExecuting = 1;
      |               intervalTestsAgrState = setInterval(getTestsAgrInfo, 200);
      |             }
      |             const resultJson = await startJsonTests();
      |             console.log('-- response -------------------------------- isTestsExecuting =',isTestsExecuting);
      |             console.log(resultJson);
      |             isTestsExecuting = 0;
      |             console.log('------------------------------------------- isTestsExecuting =',isTestsExecuting);
      |        }
      |
      |        function btnSelectAll(){
      |           console.log('select all tests');
      |           const tests_container = document.getElementById("test_list");
      |           Array.from(tests_container.querySelectorAll("input[type=checkbox]"))
      |             .map((elem) => elem.checked = true)
      |        }
      |
      |        function btnUnselectAll(){
      |          console.log('unselect all tests');
      |          const tests_container = document.getElementById("test_list");
      |          Array.from(tests_container.querySelectorAll("input[type=checkbox]"))
      |             .map((elem) => elem.checked = false)
      |        }
      |
      |          async function startJsonShowReportHtml() {
      |             console.log('Start ajax function.');
      |             console.log('-- request ----------------------------------------------');
      |             console.log(reportJsonParams);
      |             console.log('---------------------------------------------------------');
      |               const response = await fetch(`1/show`, {
      |                    method: "POST",
      |                    headers: {
      |                     'Accept': 'text/html',
      |                     'Content-Type': 'text/html'
      |                    },
      |                    body: reportJsonParams
      |                  });
      |               //const resp_message = document.getElementById("resp_message");
      |               const report_container = document.getElementById("report_rows");
      |               report_container.innerHTML = "";
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('-- error ----------------------------------------------');
      |                     //myStopFunction();
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     console.log('Response text = ',responseText);
      |                     //resp_message.className = 'error_msg';
      |                     report_container.innerHTML = `ERROR:<br/> ${responseText}`;
      |                     console.log('---------------------------------------------------------');
      |                     return response;
      |                   } else {
      |                     //myStopFunction();
      |                     //getTestsState();
      |                     //resp_message.classList.remove('error_msg');
      |                     responseText = await response.text();
      |                     report_container.innerHTML = responseText;
      |                     return response;
      |                   }
      |        }
      |
      |        function getExportPb() {
      |          if (isExportExecuting == 1) {
      |          getExportProgress();
      |          }
      |        }
      |
      | async function getExportProgress() {
      |               console.log('getExportProgress exportID = ',exportID);
      |               const response = await fetch(`1/export/status/`+exportID, {method: "GET"});
      |               responseStatus = response.status;
      |               var p_export_percent = document.getElementById("export_prcnt");
      |               var p_export_pb      = document.getElementById("export_pb");
      |               var p_for_link       = document.getElementById("p_for_link");
      |               var a_export_link    = document.getElementById("export_link");
      |
      |                   if (responseStatus == 400) {
      |                     console.log('-- error ----------------------------------------------');
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     p_for_link.innerText = `ERROR:<br/> ${JSON.parse(responseText).message}`;
      |                     myStopFunction();
      |                     console.log('Response text = ',responseText);
      |                     console.log('---------------------------------------------------------');
      |                   } else {
      |                     responseText = await response.text();
      |                     const response_json = JSON.parse(responseText);
      |                     const exp_status = response_json.status;
      |                     const exp_prcnt  = response_json.percent;
      |                     console.log("exp_status=",exp_status,"exp_prcnt=",exp_prcnt);
      |                     switch (exp_prcnt) {
      |                                         case 0:
      |                                           p_export_percent.innerText  = "0 %";
      |                                           p_export_pb.innerText  = "..........";
      |                                           break;
      |                                         case 10:
      |                                           p_export_percent.innerText  = "10 %";
      |                                           p_export_pb.innerText   = "|.........";
      |                                           break;
      |                                         case 20:
      |                                           p_export_percent.innerText  = "20 %";
      |                                           p_export_pb.innerText  = "||........";
      |                                           break;
      |                                         case 30:
      |                                           p_export_percent.innerText  = "30 %";
      |                                           p_export_pb.innerText  = "|||.......";
      |                                           break;
      |                                         case 40:
      |                                           p_export_percent.innerText  = "40 %";
      |                                           p_export_pb.innerText  = "||||......";
      |                                           break;
      |                                         case 50:
      |                                           p_export_percent.innerText  = "50 %";
      |                                           p_export_pb.innerText  = "|||||.....";
      |                                           break;
      |                                         case 60:
      |                                           p_export_percent.innerText  = "60 %";
      |                                           p_export_pb.innerText  = "||||||....";
      |                                           break;
      |                                         case 70:
      |                                           p_export_percent.innerText  = "70 %";
      |                                           p_export_pb.innerText  = "|||||||...";
      |                                           break;
      |                                         case 80:
      |                                           p_export_percent.innerText  = "80 %";
      |                                           p_export_pb.innerText  = "||||||||..";
      |                                           break;
      |                                         case 90:
      |                                           p_export_percent.innerText  = "90 %";
      |                                           p_export_pb.innerText  = "|||||||||.";
      |                                           break;
      |                                         case 100:
      |                                           p_export_percent.innerText  = "100 %";
      |                                           p_export_pb.innerText  = "||||||||||";
      |                                           if (exp_status == "Success") {
      |                                            a_export_link.href   = '/export/'+exportLinkHref;
      |                                            a_export_link.hidden = false;
      |                                            p_for_link.hidden = true;
      |                                            myStopFunction();
      |                                           } else {
      |                                            p_for_link.hidden = false;
      |                                           }
      |                                           break;
      |                                         default:
      |                                           p_export_pb.innerText  = "..........";
      |                                       }
      |                   }
      |                   return response;
      |        }
      |
      |
      |         async function startExportReport1() {
      |             console.log('Start ajax function.');
      |             console.log('-- request export----------------------------------------');
      |             console.log(reportJsonParams);
      |             console.log('---------------------------------------------------------');
      |
      |             var p_export_percent = document.getElementById("export_prcnt");
      |             var p_export_pb      = document.getElementById("export_pb");
      |             var p_for_link       = document.getElementById("p_for_link");
      |             var a_export_link    = document.getElementById("export_link");
      |
      |             p_export_percent.hidden=true;
      |             p_export_pb.hidden=true;
      |             p_for_link.hidden=true;
      |             a_export_link.hidden=true;
      |
      |               const response = await fetch(`1/export`, {
      |                    method: "POST",
      |                    headers: {
      |                     'Accept': 'application/json',
      |                     'Content-Type': 'application/json'
      |                    },
      |                    body: reportJsonParams
      |                  });
      |
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     console.log('Response text = ',responseText);
      |                     report_container.innerHTML = `ERROR:<br/> ${responseText}`;
      |                     isExportExecuting = 0;
      |                     exportID = "";
      |                     console.log('---------------------------------------------------------');
      |                     return response;
      |                   } else {
      |                     responseText = await response.text();
      |                     console.log(responseText);
      |                     const response_json = JSON.parse(responseText);
      |                     exportID = response_json.key;
      |                     exportLinkHref = response_json.fileName;
      |                     console.log("exportID = ",exportID,"exportLinkHref=",exportLinkHref);
      |                     isExportExecuting = 1;
      |                     p_export_percent.hidden = false;
      |                     p_export_pb.hidden      = false;
      |                     //p_for_link.hidden       = false;
      |                     //a_export_link.hidden    = false;
      |                     intervalPb = setInterval(getExportPb, 1000);
      |                     return response;
      |                   }
      |
      |         }
      |
      |          async function startJsonShowReportJson() {
      |             console.log('Start ajax function.');
      |             console.log('-- request ----------------------------------------------');
      |             console.log(reportJsonParams);
      |             console.log('---------------------------------------------------------');
      |
      |               var report_table = document.getElementById("report_table");
      |               report_table.innerHTML = "";
      |
      |               const response = await fetch(`1/show`, {
      |                    method: "POST",
      |                    headers: {
      |                     'Accept': 'application/json',
      |                     'Content-Type': 'application/json'
      |                    },
      |                    body: reportJsonParams
      |                  });
      |
      |               report_table.innerHTML = `<thead><tr id="rep_table_header"></tr></thead>`;
      |
      |               const report_table_header = document.getElementById("rep_table_header");
      |
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     console.log('Response text = ',responseText);
      |                     report_container.innerHTML = `ERROR:<br/> ${responseText}`;
      |                     console.log('---------------------------------------------------------');
      |                     return response;
      |                   } else {
      |                     responseText = await response.text();
      |                     const response_json = JSON.parse(responseText);
      |                     const divSummary = document.getElementById("div_summary");
      |
      |                     const e_execFetchHms = response_json.summary.execFetchSummaryMs;
      |                     const t_execFetchMs  = response_json.summary.execFetchDataMs;
      |                     const totalRows      = response_json.summary.totalRows;
      |                     const currPage       = response_json.summary.currPage;
      |                     const pageCnt        = response_json.summary.pageCnt;
      |
      |                     divSummary.innerHTML  = `<div>(exec-fetch) Summary : ${e_execFetchHms} ms.</div>`;
      |                     divSummary.innerHTML += `<div>(exec-fetch) Data : ${t_execFetchMs} ms.</div>`;
      |                     divSummary.innerHTML += `<div>totalRows : ${totalRows} </div>`;
      |                     divSummary.innerHTML += `<div>currPage : ${currPage} </div>`;
      |                     divSummary.innerHTML += `<div>pageCnt : ${pageCnt} </div>`;
      |
      |                     const hiTotal     = response_json.headInfo.totalCntRows;
      |                     const hicntNoPd   = response_json.headInfo.cntNoPd;
      |                     const cntPdError  = response_json.headInfo.cntPdError;
      |                     const cntAccError = response_json.headInfo.cntAccError;
      |
      |
      |                     var svod = document.getElementById("svod");
      |                     svod.innerHTML   = " ";
      |                     svod.innerHTML   = `<div>Общее число записей : ${hiTotal} </div>`;
      |                     svod.innerHTML  += `<div>Отсутствует ПД      : ${hicntNoPd} </div>`;
      |                     svod.innerHTML  += `<div>Кол-во. ошибок ПД   : ${cntPdError} </div>`;
      |                     svod.innerHTML  += `<div>Кол-во. ошибок ЛС   : ${cntAccError} </div>`;
      |
      |
      |                     for (var i = 0; i < response_json.header.length; i++) {
      |                         let hdr = response_json.header[i];
      |                         report_table_header.innerHTML += `<td>` + hdr.name + `</td>`;
      |                     }
      |
      |                     var innerTable = '';
      |                     for (var i = 0; i < response_json.data.length; i++) {
      |                     innerTable += "<tr>"
      |                         let r = response_json.data[i];
      |                         innerTable += "<td>" +  r.rn + "</td>"
      |                         innerTable += "<td>" +  r.omsu_name + "</td>"
      |                         innerTable += "<td>" +  r.org_name + "</td>"
      |                         innerTable += "<td>" +  r.ogrn + "</td>"
      |                         innerTable += "<td>" +  r.ls_number + "</td>"
      |                         innerTable += "<td>" +  r.acc_status + "</td>"
      |                         innerTable += "<td>" +  r.gis_error + "</td>"
      |                         innerTable += "<td>" +  r.pd_label + "</td>"
      |                         innerTable += "<td>" +  r.gis_unique_number + "</td>"
      |                         innerTable += "<td>" +  r.pd_status + "</td>"
      |                         innerTable += "<td>" +  r.pd_gis_error + "</td>"
      |                         innerTable += "<td>" +  r.address + "</td>"
      |                     innerTable += "</tr>"
      |                     }
      |                     report_table.innerHTML += innerTable;
      |                     return response;
      |                   }
      |        }
      |
      |async function btnShowReport(act){
      |          console.log('-- btnShowReport -------------------- ');
      |          var jsonParamsStr;
      |
      |          var selectedOrgsId = [];
      |          var selectedErrlsTxt = [];
      |          var selectedErrlsTxtCnt = 0;
      |          var selectedOmsuId = [];
      |          var selectedExtpd  = [];
      |          var selectedErrpd  = [];
      |          var selectedErrls  = [];
      |          var selectedPeriod = [];
      |
      |          var orgOptions = document.getElementById("orgs-select").options;
      |          for (var i = 0; i < orgOptions.length; i++) {
      |                  if (orgOptions[i].selected)
      |                      selectedOrgsId.push(orgOptions[i].value);
      |              };
      |
      |          var errlstxtOptions = document.getElementById("errlstxt-select").options;
      |
      |          for (var i = 0; i < errlstxtOptions.length; i++) {
      |                  if (errlstxtOptions[i].selected){
      |                  selectedErrlsTxtCnt = selectedErrlsTxtCnt + 1;
      |                  }
      |          }
      |
      |          console.log("selectedErrlsTxtCnt=",selectedErrlsTxtCnt);
      |
      |          if (selectedErrlsTxtCnt > 0) {
      |             for (var i = 0; i < errlstxtOptions.length; i++) {
      |                  if (errlstxtOptions[i].selected){
      |                       selectedErrlsTxt.push('"' + errlstxtOptions[i].text.replaceAll('"',"\\\"") + '"');
      |                      }
      |              }
      |          }
      |
      |          console.log("selectedErrlsTxt=",selectedErrlsTxt);
      |
      |          var omsuOptions = document.getElementById("omsu-select").options;
      |          for (var i = 0; i < omsuOptions.length; i++) {
      |                  if (omsuOptions[i].selected)
      |                      selectedOmsuId.push(omsuOptions[i].value);
      |              };
      |
      |          var extpdOptions = document.getElementById("extpd-select").options;
      |          for (var i = 0; i < extpdOptions.length; i++) {
      |                  if (extpdOptions[i].selected)
      |                      selectedExtpd.push(extpdOptions[i].value);
      |              };
      |
      |          var errpdOptions = document.getElementById("errpd-select").options;
      |          for (var i = 0; i < errpdOptions.length; i++) {
      |                  if (errpdOptions[i].selected)
      |                      selectedErrpd.push(errpdOptions[i].value);
      |              };
      |
      |          var errlsOptions = document.getElementById("errls-select").options;
      |          for (var i = 0; i < errlsOptions.length; i++) {
      |                  if (errlsOptions[i].selected)
      |                      selectedErrls.push(errlsOptions[i].value);
      |              };
      |
      |          var periodOptions = document.getElementById("period-select").options;
      |          for (var i = 0; i < periodOptions.length; i++) {
      |                  if (periodOptions[i].selected)
      |                      selectedPeriod.push(periodOptions[i].value);
      |              };
      |
      |           var pageRowsCnt = document.getElementById("pagerowscnt");
      |           var pageNum     = document.getElementById("pagenum");
      |           //console.log("pageRowsCnt=",pageRowsCnt.value);
      |
      |          jsonParamsStr = "{\"org\" : ["  + selectedOrgsId +
      |                      "], \"omsu\" : ["   + selectedOmsuId +
      |                      "], \"errlstxt\" : ["   + selectedErrlsTxt +
      |                      "], \"existpd\" : "  + selectedExtpd  +
      |                      " , \"errpd\" : "    + selectedErrpd  +
      |                      " , \"errls\" : "    + selectedErrls  +
      |                      " , \"period\" : "   + selectedPeriod  +
      |                      " , \"page_num\" : " + pageNum.value +
      |                      " , \"page_cnt\" : " + pageRowsCnt.value + "}";
      |
      |          reportJsonParams = JSON.parse(JSON.stringify(jsonParamsStr));
      |          console.log("JSON for AJAX",reportJsonParams);
      |          //const resultJson = await startJsonShowReportHtml();
      |
      |          var resultJson;
      |          if (act == 1){
      |          resultJson = await startJsonShowReportJson();
      |          } else {
      |          console.log('call ajax for export');
      |          resultJson = await startExportReport1();
      |          }
      |
      |          console.log('-- startJsonShowReportHtml res =',resultJson);
      |        }
      |
      |function tdCellClick(tdCell){
      |  console.log("click cell.id = ",tdCell.id);
      |  var elms = tdCell.id.split('_');
      |  var period = parseInt(elms[0])*12 + parseInt(elms[3]) - 1;
      |  const thisLsErrorText = tdCell.dataset.lserrortext;
      |  console.log("click thisLsErrorText = ",thisLsErrorText);
      |  console.log("thisLsErrorText.length = ",thisLsErrorText.length);
      |  var selectedErrls = [];
      |
      |  if (thisLsErrorText.length === 1){
      |    selectedErrls.push(0);
      |  } else {
      |    selectedErrls.push(-1);
      |  }
      |
      |  console.log("selectedErrls[0] = ",selectedErrls[0]);
      |
      |   //var errlsOptions = document.getElementById("errls-select").options;
      |   //       for (var i = 0; i < errlsOptions.length; i++) {
      |   //               if (errlsOptions[i].selected)
      |   //                   selectedErrls.push(errlsOptions[i].value);
      |   //           };
      |
      |  window.open('/report/1?period=' + period + '&omsu='+ elms[1] +'&org='+ elms[2] +'&errls='+ selectedErrls[0] +'&existpd=1&page_num=1&page_cnt=100'+'&errlstxt='+thisLsErrorText);
      |}
      |
      |function selCell(cell) {
      |  const cellValue = cell.dataset.intervalue;
      |  const parts  = cell.id.split('_');
      |  const values = cellValue.split('-');
      |
      |  var selNyear = parts[0];
      |  var selOmsu  = parts[1];
      |  var selOrg   = parts[2];
      |  var selMonth = parts[3];
      |
      |  var v_pd_err_prcnt = values[0];
      |  var v_pd_total     = values[1];
      |  var v_pd_errors    = values[2];
      |
      |    r2_omsu_name.innerText      =  selOmsu;
      |    r2_org_name.innerText       =  selOrg
      |    r2_year.innerText           =  selNyear + " год., id = " + (parseInt(selNyear)*12 + parseInt(selMonth) - 1);
      |    r2_pd_total.innerText       = v_pd_total;
      |    r2_pd_error.innerText       = v_pd_errors;
      |    r2_pd_prcnt_error.innerText = v_pd_err_prcnt;
      |
      |}
      |
      |
      | function unselCell(cell) {
      |    r2_omsu_name.innerText      = "";
      |    r2_org_name.innerText       = "";
      |    r2_year.innerText           = "";
      |    r2_pd_total.innerText       = "";
      |    r2_pd_error.innerText       = "";
      |    r2_pd_prcnt_error.innerText = "";
      | }
      |
      |async function btnShowReport2(){
      |          console.log('-- btnShowReport2 -------------------- ');
      |          var jsonParamsStr;
      |
      |          var selectedOrgsId      = [];
      |          var selectedErrlsTxt    = [];
      |          var selectedErrlsTxtCnt = 0;
      |          var selectedOmsuId = [];
      |          var selectedExtpd = [];
      |          var selectedErrls  = [];
      |          var selectedYear = [];
      |
      |          var orgOptions = document.getElementById("orgs-select").options;
      |          for (var i = 0; i < orgOptions.length; i++) {
      |                  if (orgOptions[i].selected)
      |                      selectedOrgsId.push(orgOptions[i].value);
      |              };
      |
      |          var errlstxtOptions = document.getElementById("errlstxt-select").options;
      |
      |          for (var i = 0; i < errlstxtOptions.length; i++) {
      |                  if (errlstxtOptions[i].selected){
      |                  selectedErrlsTxtCnt = selectedErrlsTxtCnt + 1;
      |                  }
      |          }
      |
      |          console.log("selectedErrlsTxtCnt=",selectedErrlsTxtCnt);
      |
      |          if (selectedErrlsTxtCnt > 0) {
      |             for (var i = 0; i < errlstxtOptions.length; i++) {
      |                  if (errlstxtOptions[i].selected){
      |                       selectedErrlsTxt.push('"' + errlstxtOptions[i].text.replaceAll('"',"\\\"") + '"');
      |                      }
      |              }
      |          }
      |
      |          console.log("selectedErrlsTxt=",selectedErrlsTxt);
      |
      |          var omsuOptions = document.getElementById("omsu-select").options;
      |          for (var i = 0; i < omsuOptions.length; i++) {
      |                  if (omsuOptions[i].selected)
      |                      selectedOmsuId.push(omsuOptions[i].value);
      |              };
      |
      |          var extpdOptions = document.getElementById("extpd-select").options;
      |          for (var i = 0; i < extpdOptions.length; i++) {
      |                  if (extpdOptions[i].selected)
      |                      selectedExtpd.push(extpdOptions[i].value);
      |              };
      |
      |
      |          var errlsOptions = document.getElementById("errls-select").options;
      |          for (var i = 0; i < errlsOptions.length; i++) {
      |                  if (errlsOptions[i].selected)
      |                      selectedErrls.push(errlsOptions[i].value);
      |              };
      |
      |          var periodOptions = document.getElementById("nyear-select").options;
      |          for (var i = 0; i < periodOptions.length; i++) {
      |                  if (periodOptions[i].selected)
      |                      selectedYear.push(periodOptions[i].value);
      |              };
      |
      |           var pageRowsCnt = document.getElementById("pagerowscnt");
      |           var pageNum     = document.getElementById("pagenum");
      |           //console.log("pageRowsCnt=",pageRowsCnt.value);
      |
      |          jsonParamsStr = "{\"org\" : ["  + selectedOrgsId +
      |                      "], \"omsu\" : ["   + selectedOmsuId +
      |                      "], \"errlstxt\" : ["   + selectedErrlsTxt +
      |                      "], \"existpd\" : "  + selectedExtpd  +
      |                      " , \"errls\" : "    + selectedErrls  +
      |                      " , \"nyear\" : "   + selectedYear  +
      |                      " , \"page_num\" : " + pageNum.value +
      |                      " , \"page_cnt\" : " + pageRowsCnt.value + "}";
      |
      |          reportJsonParams = JSON.parse(JSON.stringify(jsonParamsStr));
      |          console.log("JSON for AJAX",reportJsonParams);
      |
      |          var resultJson;
      |          resultJson = await startJsonShowReportJson2();
      |
      |          console.log('-- startJsonShowReport2Html res =',resultJson);
      |        }
      |
      |        function getCellColor(cellValueStr){
      |        if (cellValueStr == "0-0-0") {
      |         return cWhite;
      |        }
      |        var pte = cellValueStr.split('-');
      |        var cellValue = parseInt(pte[0]);
      |          if (cellValue > 75)
      |            return cRed;
      |          else if (cellValue > 50)
      |            return cLightRed;
      |          else if (cellValue > 25)
      |            return cLightGreen
      |          else
      |            return cGreen;
      |        }
      |
      | async function startJsonShowReportJson2() {
      |             console.log('Start ajax function.');
      |             console.log('-- request ----------------------------------------------');
      |             console.log(reportJsonParams);
      |             console.log('---------------------------------------------------------');
      |
      |               var report_table = document.getElementById("report_table");
      |               report_table.innerHTML = "";
      |
      |               const response = await fetch(`2/show`, {
      |                    method: "POST",
      |                    headers: {
      |                     'Accept': 'application/json',
      |                     'Content-Type': 'application/json'
      |                    },
      |                    body: reportJsonParams
      |                  });
      |
      |               report_table.innerHTML = `<thead><tr id="rep_table_header"></tr></thead>`;
      |
      |               const report_table_header = document.getElementById("rep_table_header");
      |
      |               responseStatus = response.status;
      |                   if (responseStatus == 400) {
      |                     console.log('Response status = ',responseStatus);
      |                     responseText = await response.text();
      |                     console.log('Response text = ',responseText);
      |                     report_container.innerHTML = `ERROR:<br/> ${responseText}`;
      |                     console.log('---------------------------------------------------------');
      |                     return response;
      |                   } else {
      |                     responseText = await response.text();
      |                     const response_json = JSON.parse(responseText);

      |                     for (var i = 0; i < response_json.header.length; i++) {
      |                         let hdr = response_json.header[i];
      |                         report_table_header.innerHTML += `<td>` + hdr.name + `</td>`;
      |                     }
      |
      |                     var innerTable = '';
      |                     for (var i = 0; i < response_json.data.length; i++) {
      |                     innerTable += "<tr>"
      |                         let r = response_json.data[i];
      |
      |                         var tdCellId = r.nyear + "_" + r.id_omsu + "_" + r.id_voc_agent;
      |                         console.log("tdCellId = ",tdCellId," r_ls_error = ",r.ls_error);

      |                         innerTable += "<td>" +  r.rn + "</td>"
      |                         innerTable += "<td>" +  r.omsu_name + "</td>"
      |                         innerTable += "<td>" +  r.org_name + "</td>"
      |                         innerTable += "<td>" +  r.ls_error + "</td>"
      |
      |                         /*
      |                         var lse0 = r.ls_error.replaceAll('"',"\\\"");
      |                         console.log("lse0=",lse0);
      |                         var lse = lse0.replaceAll('/',"%2F");
      |                         console.log("lse=",lse);
      |                         */
      |                         //var lse = r.ls_error.replaceAll('"',"\\\"");
      |                         var lse = r.ls_error.replaceAll('"',"&quot;");
      |                         console.log(" lse = ",lse);
      |
      |
      |innerTable += '<td id= "' + tdCellId + "_" + 1  +'"  bgcolor='+ getCellColor(r.r_1_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_1_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 2  +'"  bgcolor='+ getCellColor(r.r_2_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_2_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 3  +'"  bgcolor='+ getCellColor(r.r_3_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_3_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 4  +'"  bgcolor='+ getCellColor(r.r_4_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_4_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 5  +'"  bgcolor='+ getCellColor(r.r_5_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_5_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 6  +'"  bgcolor='+ getCellColor(r.r_6_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_6_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 7  +'"  bgcolor='+ getCellColor(r.r_7_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_7_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 8  +'"  bgcolor='+ getCellColor(r.r_8_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_8_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 9  +'"  bgcolor='+ getCellColor(r.r_9_koef_totcnt_errcnt)  +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_9_koef_totcnt_errcnt  + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 10 +'"  bgcolor='+ getCellColor(r.r_10_koef_totcnt_errcnt) +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_10_koef_totcnt_errcnt + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 11 +'"  bgcolor='+ getCellColor(r.r_11_koef_totcnt_errcnt) +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_11_koef_totcnt_errcnt + ' data-lserrortext="'+ lse +'"></td>'
      |innerTable += '<td id= "' + tdCellId + "_" + 12 +'"  bgcolor='+ getCellColor(r.r_12_koef_totcnt_errcnt) +' onmouseover="selCell(this)" onmouseout="unselCell(this)" onclick="tdCellClick(this)" data-intervalue=' + r.r_12_koef_totcnt_errcnt + ' data-lserrortext="'+ lse +'"></td>'
      |
      |                         innerTable += "<td>" +  r.total + "</td>"
      |
      |                     innerTable += "</tr>"
      |                     }
      |                     report_table.innerHTML += innerTable;
      |                     return response;
      |                   }
      |        }
      |
      |    </script>
      |""".replace("\r", "").stripMargin

}
