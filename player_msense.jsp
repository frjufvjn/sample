<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<meta name="viewport" content="width=device-width">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <link rel="stylesheet" href="css/waveform.css" />
        <link rel="stylesheet" href="css/control.css" />
        <link rel="stylesheet" href="css/sttarea.css" />
 		<link rel="stylesheet" href="css/sub-menu.css" />
 		
 		<!-- jQuery -->
		<script type='text/javascript' src='../chart/lib/jquery-1.11.1.js'></script>  
		
		<script type='text/javascript' src='./src/wmp.js?version=1.2'></script>  
		<script type='text/javascript' src='./src/content.js?version=1.2'></script> 
		<script type='text/javascript' src='./src/waveform.js'></script> 
		<script type='text/javascript' src='./src/util.js'></script> 
    </head>
    <body>
    	<div id="container" class="container">
        	
			<div id="wmp"></div>
			
            <div id="demo">

            	<!-- waveform -->
                <div id='waveform' >
                	<img id="loading" src="./images/loading.gif" style="display:none; z-index:1000; display:block;position:absolute;top:10%;left:35%;transform:translate(-10%,-35%); " />
                </div>

				<!-- timer area -->
				<div class="timer">
					<span class="track-start-time" ></span>
					<span class="track-end-time" ></span>
				</div>
				
				<!-- control button area -->
				<div class="control">
				
	                <div class="button">
						
						<!-- data-action: example/trivia.js,  class: bootstrap.min.css -->
	                    <button class="btn-back"></button>
	
	                    <button class="btn-play"></button>
	                    
	                    <button class="btn-stop"></button>
	                    
	                    <button class="btn-forth"></button>
	                    
	                    <div class="volume">
	                    	<img src="images/volume.png" alt="volum-up icon" />
	                    	<input type="range" id="volume-slider" class="slider" min="-4000" max="0" value="0" step="1000">
		                </div>
	                </div>
		                
		            <div class="setting">   
		                <img src="images/setting.png" class="btn_setting" onclick = "document.getElementById('light').style.display='block';document.getElementById('fade').style.display='block'" alt="setting" />
		                <div id="light" class="setting_content"> 
		                
		                	<div class="speed-mode">
		                		<font>speed</font>
		                    	<form>
		                    	<input type="range" class="slider" name="slider" id="speed-slider" min="0.5" max="2.0" value="1.3" step="0.1" />
		                    	<!-- output for="slider" onforminput="value = speed-slider.valueAsNumber;">1.0</output -->
		                    	</form>
		                	</div>
		                	<hr>
		                	<div class="font-mode">
			                	<font>font size</font>
			                	<input type="button" class="button" id="font-size-down" style="background:#5b6172 url(images/font-size-down.png) 50% 50% no-repeat;" onclick="resizeFont('down')" />
              					<input type="button" class="button" id="font-size-up" style="background:#5b6172 url(images/font-size-up.png) 50% 50% no-repeat;"  onclick="resizeFont('up')" />
		                	</div>
		                </div>
		                <div id="fade" class="black_overlay" onclick = "document.getElementById('light').style.display='none';document.getElementById('fade').style.display='none'"></div>
		            </div>  
		            
		            <div id="tuning" class="tuning">
				    <img src="../images/icon_typelist.png" class="btn_tuning" onclick="openTunning('02')" alt="tuning" />
					</div>
			    </div>
		    </div> 
			    
		    <div class="sub-menu">
		    	<input type="button" class="btn_info" id="callinfo" title="call info" style="background:url('./images/callinfo.png') 50% 50% no-repeat;" onclick=openLayer(this.id) />
		    	<input type="button" class="btn_rank" id="rank" title="TOP keyword" style="background:url('./images/rank.png') 50% 50% no-repeat;" onclick=openLayer(this.id) />
		    	<input type="button" class="btn_summary" id="summary" title="summary" style="background:url('./images/summary.png') 50% 50% no-repeat;" onclick=openLayer(this.id) /> 
		    	<!--<input type="button" class="btn_memo" id="memo" title="memo" style="background:url('./images/memo.png') 50% 50% no-repeat;" onclick=openLayer(this.id) />-->
		    	<input type="button" class="btn_history" id="history" title="contact history" style="background:url('./images/history.png') 50% 50% no-repeat;" onclick=openLayer(this.id) />
		    	<input type="button" class="btn_close" id="history" title="close" style="background:url('./images/close.png') 50% 50% no-repeat;" onclick=closeLayer() />
		    </div>
		    
		    <div id="search" class="search">
                   <div class="img-wrapper"><img src="images/search.png" alt="search icon" /></div>
	            <input type="text" id="srcKey" class="srcKey" style="ime-mode:active;" onkeydown="" />
	            <div class="browser-view" style="cursor: pointer;">
	            	<button id="toggleBtn" onclick="toggle()" >STT</button>
	            	<!-- <img id="toggleBtn" src="images/split.png" style="cursor: pointer;" alt="" onclick=toggle() /> -->
	            </div>
	            <!-- <div class="browser-view" style="cursor: pointer;"><img id="toggleBtn" src="images/split.png" style="cursor: pointer;" alt="" onclick=toggle() /></div> -->
			</div>
			
			
           </div>
		
		<script>
			var host = "http://"+location.host+"/VSENS_KEPCO/";
			var path = "temp/";
			var file_name = "";
			var rtx = "N";
			var meta = {
					file_len: 0, 
					cust_no: 0,
					agent_id: 0,
					ta_data: "",
					stt_data: "",
					reckey: '',   
					contact_id: '',   
					start_time: '', 
					keyword: "", 
					user_grade: "",
					play_user_id: "", 
					cust_name: "",
					ucid: "",
					incall_no: ""
				};
			
			var controls =  {
		    		btnPlay: document.querySelector('.btn-play'),
		    	    btnStop: document.querySelector('.btn-stop'),
		    	    timeElapsed: document.querySelector('.track-start-time'),
		    	    timeTotal: document.querySelector('.track-end-time'),
		    	    btnPrevious: document.querySelector('.btn-back'),
		    	    btnNext: document.querySelector('.btn-forth'),
		    	    btnVolumeDown: document.querySelector('.btn-volume-down'),
		    	    btnVolumeUp: document.querySelector('.btn-volume-up'),
		    	    //volume: document.querySelector('.player__volume-info')
		    	};
			
			var width=500, height=680;
			var open_stt_top='445px', open_src_top='445px';
			var close_stt_top='255px', close_src_top='261px';
			
			$(document).ready(function(){
				document.body.oncontextmenu = function(){return false;};
				
				var query = location.search.substring(1);
				var parameters = {};
				var keyValues = query.split(/&/);
				for (var prop in keyValues) {
				    var keyValuePairs = keyValues[prop].split(/=/);
				    var key = keyValuePairs[0];
				    var value = keyValuePairs[1];
				    parameters[key] = value;
				}
				//meta.contact_id   = parameters['contact_id']; //parent.contact_id;
				//meta.start_time   = parameters['start_time']; //parent.start_time;
				meta.reckey       = parameters['reckey']; //parent.rec_key;
				meta.keyword      = parameters['keyword'];
				
				file_name = host+path+meta.reckey+".wav"; 
				
				// control buttons event
				controls.btnPlay.addEventListener('click', function() {
					wmp.play();
				});
				controls.btnStop.addEventListener('click', function() {
					wmp.stop();
				});
				controls.btnPrevious.addEventListener('click', function() {
					wmp.seekTo(wmp.getCurrentTime()-10);
				});
				controls.btnNext.addEventListener('click', function() {
					wmp.seekTo(wmp.getCurrentTime()+10);
				});

				
				stt.loadText(host, meta.reckey);
				//wmp.loadAudio(host, meta.reckey);
			});
		    	
			function createWaveform(wavedata) {
				new Waveform({
        			  container: document.getElementById("waveform"),
        			  innerColor: "#00b7a5",
        			  width: 500,
        			  height: 124,
        			  data: wavedata
        			}); 
        		 
        		document.getElementById("waveform").addEventListener("click", function(e){
        			var perc = e.offsetX / $(this).width();
        		  	var pos = wmp.getDuration() * perc;
        		  	wmp.seekTo(pos);
				});
        		
        		
        		// Windows Media Player object create
        		file_name = host+path+meta.reckey+".wav"; 
				generateWindowsMediaPlayer("wmp","MediaPlayer", file_name); //temporary
				// timelaps
				window.setInterval("whilePlaying()",100); 
			}
			
			$(document).keydown(function(){
				if(event.keyCode==32){ //space bar
					wmp.play();
				}else if(event.keyCode==13){ //enter 
					search();
					//javascript:if(event.keyCode==13){ search(); }
				} 
					
	        });
			
			// move by click STT
			function sttJump(start_point){
				//seekTo() : sec
				wmp.seekTo( start_point/1000 );
			}
			
			// search
			function search(){
				var keyword = document.getElementById('srcKey').value;
				keyword = keyword.replace(/\s+/g, '');
				
				stt.removeMark();
				
				var element = document.getElementById('passage-text');
				var elms = element.querySelectorAll('[data-start]');
				var str = "", current_span = "";
				for (var i=0; i<elms.length; i++) {
					str = (elms[i].innerHTML).replace(/\s+/g, '');;
					current_span = stt.words[i];
					current_span.element.style.backgroundColor = "#F7F1E2";
					if(keyword != '' && str.indexOf(keyword) > -1){
						stt.mark(current_span.begin/meta.duration); // ms 
						
						current_span = stt.words[i];
						current_span.element.style.backgroundColor = "#FFBA46";
					};
				}
				
				/* if(rtx=="Y"){
					var element_tx = document.getElementById('passage-text-tx');
					var elms_tx = element_tx.querySelectorAll('[data-start]');
					for (var i=0; i<elms_tx.length; i++) {
						str = (elms_tx[i].innerHTML).replace(/\s+/g, '');;
						current_span = stt.words_tx[i];
						current_span.element.style.backgroundColor = "#F7F1E2";
						if(keyword != '' && str.indexOf(keyword) > -1){
							
							stt.mark(current_span.begin*1000/meta.file_len); // ms 
							
							current_span = stt.words_tx[i];
							current_span.element.style.backgroundColor = "#e0ca8d"; //"#FFBA46";
						};
					}
					
					var element_rx = document.getElementById('passage-text-rx');
					var elms_rx = element_rx.querySelectorAll('[data-start]');
					for (var i=0; i<elms_rx.length; i++) {
						str = (elms_rx[i].innerHTML).replace(/\s+/g, '');;
						current_span = stt.words_rx[i];
						current_span.element.style.backgroundColor = "#F7F1E2";
						if(keyword != '' && str.indexOf(keyword) > -1){
							
							stt.mark(current_span.begin*1000/meta.file_len); // ms 
							
							current_span = stt.words_rx[i];
							current_span.element.style.backgroundColor = "#e0ca8d"; //"#FFBA46";
						};
					}
				} */
				
			}		
			

			// font setting
			function resizeFont(gubun){
				var el = document.getElementById('passage-text');
				var style = window.getComputedStyle(el, null).getPropertyValue('font-size');
				var fontSize = parseFloat(style); 

				if(gubun=='up' && fontSize<30){
					el.style.fontSize = (fontSize+1) + 'px';
				}else if(gubun=='down' && fontSize>10){
					el.style.fontSize = (fontSize-1) + 'px';
				}
			}
			
			
			// click event for sub-menu
			function openLayer(id){
				//search,stt area sizing
				/* var el_stt = document.getElementById('passage-text');
				if(el_stt!= null){
					el_stt.style.height = (height-490)+'px'; // '160px';
					el_stt.style.top = '450px';
				}
				var el_src = document.getElementById('search');
				el_src.style.top = '450px'; */
				if(document.getElementById('passage-text')!=null){
					document.getElementById('passage-text').style.height=(height-510)+'px';
					document.getElementById('passage-text').style.top=open_stt_top;
				}
				
				if(rtx=="Y"){
					if(document.getElementById('passage-text-tx')!=null){
						document.getElementById('passage-text-tx').style.height=(height-510)+'px';
						document.getElementById('passage-text-tx').style.top=open_stt_top;
					}
					if(document.getElementById('passage-text-rx')!=null){
						document.getElementById('passage-text-rx').style.height=(height-510)+'px';
						document.getElementById('passage-text-rx').style.top=open_stt_top;
					}
				}
				document.getElementById("search").style.top = open_src_top;
				
				//iframe src url 
				var url;
				switch(id){
					case 'callinfo':
						url = './call_info.html';
						break;
					case 'rank':
						url = './top_keyword.html';
						break;
					case 'summary':
						url = './summary.html';
						break;
					case 'memo':
						url = './memo.html';
						break;
					case 'history':
						url = './history_chart.html';
						break;
				}
				
				if(document.getElementById('sub-container') == null){
					// div create
					var container = document.createElement('div');
					container.setAttribute('id', 'sub-container');
					container.style.height = '180px';
					document.getElementById('container').appendChild(container);
					// iframe create
					var iframe = document.createElement('iframe');
					iframe.frameBorder=0;
					iframe.height='180px';
					iframe.id="if-sub";
					
					document.getElementById('sub-container').appendChild(iframe);
				}
				var frame = document.getElementById('if-sub');
				frame.src = url;
				var frameDoc = frame.contentWindow.document;
				//frameDoc.calling();
			}
			
			function closeLayer(){
				if(document.getElementById('sub-container') != null){
					$('#sub-container').remove();
					$('#if-sub').remove();
				}
				
				/* var el_stt = document.getElementById('passage-text');
				var el_src = document.getElementById('search');
				//var height = 670; //parent.$pan_player.getAttribute("height");
				el_stt.style.height = (height-300)+'px'; //'340px';
				el_stt.style.top = '255px';
				el_src.style.top = '261px'; */
				
				document.getElementById('passage-text').style.height=(height-320)+'px';
				document.getElementById('passage-text').style.top=close_stt_top;
				document.getElementById('passage-text-stt').style.height=(height-320)+'px';
				document.getElementById('passage-text-stt').style.top=close_stt_top;
				document.getElementById('search').style.top=close_src_top;
				if(rtx=="Y"){
					document.getElementById('passage-text-tx').style.height=(height-320)+'px';
					document.getElementById('passage-text-tx').style.top=close_stt_top;
					document.getElementById('passage-text-rx').style.height=(height-320)+'px';
					document.getElementById('passage-text-rx').style.top=close_stt_top;
				}
			}
			
			// history_chart.html click
			function reloadPlayer(newrec_key){
				
				meta.reckey = newrec_key;
				//parent.rec_key = reckey;
				
				wmp.empty(); //audio file empty

				stt.loadText(host, meta.reckey);
			}
			
			function toggle(){
		    	var img = document.getElementById('toggleBtn');
		    	//if(rtx=="Y"){
		    		if(img.innerHTML=='STT') { //TA->STT
		    			img.innerHTML='TA';
			    		document.getElementById("passage-text").style.visibility = "hidden";
			    		document.getElementById("passage-text-stt").style.visibility = "visible";
			    		//document.getElementById("passage-text-tx").style.visibility = "hidden";
			    		//document.getElementById("passage-text-rx").style.visibility = "hidden";
			    		stt.selectCurrentWord();
			    	} else { //STT->TA
			    		img.innerHTML='STT';
			    		document.getElementById("passage-text").style.visibility = "visible";
			    		document.getElementById("passage-text-stt").style.visibility = "hidden";
			    		//document.getElementById("passage-text-tx").style.visibility = "visible";
			    		//document.getElementById("passage-text-rx").style.visibility = "visible";
			    		stt.selectCurrentWord();
			    	}
		    	//}
		    	
		    }
			
			function openTunning(type){
				/* var param={	
						gubun_tunning : type,
						rec_key_tunning: meta.reckey,
						contact_id_tunning: meta.contact_id,
						start_time_tunning : meta.start_time,
						user_id_tunning : meta.agent_id,
						cust_no_tunning : meta.cust_no
					};
				
				if(window.opener != null) window.opener.openTunning(param); //popup
				else top.tunning_popup(param); //embed  */
				
				var params = "?rec_key="+meta.reckey+"&contact_id="+meta.contact_id+"&start_time="+meta.start_time;
				params += "&user_id="+meta.agent_id+"&cust_no="+meta.cust_no+"&gubun="+type;
				window.open("http://90.10.100.35:8090/VSENS_KEPCO/RSK/RSK080P1.xhtml"+params, "정정요청", "width=800, height=420, left=0, top=0, location=no, resizable=no, menubar=no, status=no");

			}
			
		</script>
		
		
    </body>
    
</html>    
