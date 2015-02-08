<%--
/************************************************************************
 **     $Date: $
 **   $Source: $
 **   $Author: Zsolt Grigore$
 ** $Revision: $
 ************************************************************************/
--%><%
%><%@page session="false" contentType="text/html; charset=utf-8" %><%
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling" %><%
%><sling:defineObjects />
<html>
	<head>
		<title>${resource.name}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="/etc/clientlibs/sling-depgen/css/pure.css">
		
	</head>
	<body>
		<form method="post" class="pure-form" enctype="multipart/form-data">
			<fieldset>
				<legend>Upload a dependency template</legend>
				
				<div class="pure-control-group">
					<input name="uploadfield" type="file">
				</div>
				
				<div class="pure-control-group">
					<label>Hint: Leave dependency section empty and let generator insert dependencies into the template</label>
				</div>
			</fieldset>
			<button type="submit" class="pure-button pure-button-primary">Submit</button>
		</form>
	</body>
</html>