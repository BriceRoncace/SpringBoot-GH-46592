<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="org.springframework.boot.SpringBootVersion" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>gh-46592 Home</title>
</head>
<body>
<h2>Spring Boot ${SpringBootVersion.getVersion()} is up and running on profile ${profile}.</h2>
</body>
</html>
