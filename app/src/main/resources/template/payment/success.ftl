<!DOCTYPE html>
<html lang="zh_CN">
<#include '*/header.ftl'>
<body>
    <div class="container-fluid text-center">
        <p class="bg-success">支付成功</p>
    </div>
<#include '*/footer.ftl'>
<script>
    if (self !== top) {
        window.parent.location.href='/payment/success'
    }
</script>
</body>
</html>
