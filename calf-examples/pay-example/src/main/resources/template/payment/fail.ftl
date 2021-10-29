<!DOCTYPE html>
<html lang="zh_CN">
<#include '*/header.ftl'>
<body>
<div class="container-fluid text-center">
    <p class="bg-danger">支付失败</p>
    <h3>message: <#if msg??>${msg}</#if></h3>
</div>
<#include '*/footer.ftl'>
</body>
</html>
