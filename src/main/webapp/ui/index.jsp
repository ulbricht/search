<!DOCTYPE html PUBLIC "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" "-//W3C//DTD XHTML 1.0 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<head>

<%
    if (request.getParameter("q") == null) {
        out.println("<meta http-equiv=\"refresh\" content=\"0;url=/search/public/ui?q=*&sort=updated+desc\">");
    } else {
        out.println("<meta http-equiv=\"refresh\" content=\"0;url=/search/public/ui?q="+ request.getParameter("q") +"&sort=updated+desc\">");
    }
%>

</head>
</html>
