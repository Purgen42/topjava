<script type="text/javascript">
    const i18n = [];
    <c:forEach var="key" items='<%=new String[]{"common.deleted","common.saved","common.enabled","common.disabled","common.errorStatus","common.confirm"}%>'>
    i18n["${key}"] = "<spring:message code="${key}"/>";
    </c:forEach>
    i18n["addTitle"] = '<spring:message code="${titlePrefix}.add"/>';
    i18n["editTitle"] = '<spring:message code="${titlePrefix}.edit"/>';
</script>