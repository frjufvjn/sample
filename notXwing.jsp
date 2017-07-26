<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@ page import="jedix.xwing.util.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.locus.jedi.log.*"%>
<%@ page import="com.locus.jedi.transfer.*"%>
<%@ page import="com.locus.jedi.waf.*"%>
<jsp:useBean id="jediReq" class="com.locus.jedi.waf.controller.JediRequest" scope="request" />
<jsp:useBean id="jediRes" class="com.locus.jedi.waf.controller.JediResponse" scope="request" />
<%
/*
CommonDTO common = jediReq.getCommonDTO();
if(common != null && common.getUserId() !=null && !common.getUserId().equals("???")){
			jediRes.param.addValue("user_id", common.getUserId());
			jediRes.param.addValue("clientIp", common.getClientIp());
			jediRes.param.addValue("user_name", common.getUserName());
   			jediRes.param.addValue("team_cd", common.getAttribute("team_cd"));
 			jediRes.param.addValue("err_cnt", common.getAttribute("err_cnt"));
 			jediRes.param.addValue("grade_cd", common.getAttribute("grade_cd"));
 			jediRes.param.addValue("grade_name", common.getAttribute("grade_name"));
 			jediRes.param.addValue("client_ip", common.getAttribute("client_ip"));
 			jediRes.param.addValue("server_ip", common.getAttribute("server_ip"));
			jediRes.param.addValue("extention", common.getAttribute("extention"));
			jediRes.param.addValue("cti_id", common.getAttribute("cti_id"));
			jediRes.param.addValue("login_date", common.getAttribute("login_date"));
			jediRes.param.addValue("login_time", common.getAttribute("login_time"));

			jediRes.param.addValue("server_id", common.getAttribute("server_id"));
 			jediRes.param.addValue("server_ip", common.getAttribute("server_ip"));
 			jediRes.param.addValue("server_port", common.getAttribute("server_port"));
 			jediRes.param.addValue("contextRoot", common.getAttribute("contextRoot"));
 			jediRes.param.addValue("serverUrl", common.getAttribute("serverUrl"));
}*/

JSONObject jsonStr =XwingProcessor.sendResponse(jediRes, jediRes.param); 
ErrorLogger.debug("Xwing.jsp out : "+ jsonStr.toString());
%><%=jsonStr.toString()%>
<%out.flush();%>
