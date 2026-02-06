<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>Error</title>
</head>
<body>
  <c:choose>
    <c:when test="${status == 403}">
      <div class="alert alert-warning" role="alert">
        <h2 class="alert-heading border-bottom border-warning">Access Denied</h2>
        <p>You do not have the appropriate permissions to perform this action.</p>
      </div>
    </c:when>
    <c:when test="${status == 404}">
      <div class="alert alert-warning" role="alert">
        <h2 class="alert-heading border-bottom border-warning">Page Not Found</h2>
        <p>Sorry, the resource you requested could not be found.</p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="alert alert-warning flex-fill" role="alert">
        <h3 class="alert-heading border-bottom border-warning d-flex justify-content-between align-items-center mb-0">
          <span>Invalid Request</span>
          <span class="small">${timestamp}</span>
        </h3>
      </div>

      <div class="card">
        <h5 class="card-header d-flex justify-content-between">
          <span>${status} - ${error}</span>
        </h5>
        <div class="card-body">
          An unexpected error occurred.  Please <a href="<c:url value="/" />">return home and try again later</a>.
        </div>
      </div>
    </c:otherwise>
  </c:choose>
</body>
</html>
