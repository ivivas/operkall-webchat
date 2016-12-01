<%-- 
    Document   : inicio
    Created on : 22-06-2015, 17:46:33
    Author     : mario
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!--<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Simple Time Interval Page Element Refresh using JQuery and a sprinkle of Ajax</title>
<script language="javascript" src="jquery/jquery-1.2.6.min.js"></script>
<script language="javascript" src="jquery/jquery.timers-1.0.0.js"></script>

<script type="text/javascript">

$(document).ready(function(){
   var j = jQuery.noConflict();
	j(document).ready(function()
	{
		j(".refreshMe").everyTime(5000,function(i){
			j.ajax({
			  url: "Mensaje",
			  cache: false,
			  success: function(html){
				j(".refreshMe").html(html);
			  }
			})
		})
	});
   j('.refreshMe').css({color:"red"});
});



</script>
</head>
<body>
    statico
<div class="refreshMe2">This will get Refreshed in 5 Seconds</div>
<div class="refreshMe">This will get Refreshed in 5 Seconds</div>
</body>
</html>
