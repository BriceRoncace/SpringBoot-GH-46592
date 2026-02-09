<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@page import="org.springframework.boot.SpringBootVersion" %>
<%@page import="java.lang.System" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>gh-46592 Home</title>
</head>
<body>
<h2>Spring Boot ${SpringBootVersion.getVersion()} is up and running on profile ${profile}.</h2>
<hr/>

<h4>System Properties</h4>
<ul>
<c:forEach var="entry" items="${System.getProperties()}">
  <li><b>${entry.key}</b>: ${entry.value}</li>
</c:forEach>
</ul>
</body>
</html>
