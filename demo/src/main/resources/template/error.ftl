<!DOCTYPE html>
<html lang="zh_CN">
<#include "header.ftl">
<body>
<div class="container-fluid text-center">
    <h1> ERROR !</h1>
    <h3>code: <#if code??>${code}</#if></h3>
    <h3>message: <#if msg??>${msg}</#if></h3>
</div>
<#include "footer.ftl">
</body>
</html>
