<%@page import="java.io.File"%>
<%@page import="java.io.*"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="java.net.URL"%>
<%@page language="java" contentType="text/plain; charset=utf-8" pageEncoding="utf-8"%>
<%
out.clear();
response.reset();
response.setContentType("text/plain;charset=UTF-8");
        String connid = request.getParameter("connid");
        BufferedReader in = null;

        try {
            URL obj = new URL("http://10.1.12.232:8080/POMAgent/test.jsp?connid=" + connid); // 호출할 url
            HttpURLConnection con = (HttpURLConnection)obj.openConnection();

            con.setRequestMethod("GET");

            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

            String line;
            while((line = in.readLine()) != null) { // response를 차례대로 출력
                out.print(line);
            }
            out.print(connid);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null) try { in.close(); } catch(Exception e) { e.printStackTrace(); }
        }
%>
