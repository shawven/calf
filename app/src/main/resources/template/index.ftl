<!DOCTYPE html>
<html lang="zh_CN">
<#include 'header.ftl'>
<body>
<div class="container-fluid">
    <div class="text-center">
        <h1>Hello World!</h1>
    </div>
</div>
<#include 'footer.ftl'>
</body>
</html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
</head>
<body>
<form name="punchout_form" method="post" action="https://gateway.test.95516.com/gateway/api/frontTransReq.do">
    <input type="hidden" name="bizType" value="000201">
    <input type="hidden" name="txnSubType" value="01">
    <input type="hidden" name="orderId" value="1568968840626">
    <input type="hidden" name="backUrl" value="http://shoven.nat123.net:10010/payment/notify/unionpay">
    <input type="hidden" name="signature" value="IpUfuwEMujkp4OfXq6rRIz2HYaH3Fo4ZOur861tvohxnPTNm5nSCyJAjTyYAenobzIMC7NsYSkb9TmJ/xOPhogWSjTVPoz+KjFA376S5eg2/phMFEYzzH0ExGv+P5L2T+mS8EQ8YqhnrW7bYnwkdMU3Ef7lp5WD+MvtlsglsSSbg4edr9a90of/iiZSlgVgOYSZT6XEqNYaGF41+A4bcWc1YFlzfDMAZbCw2j/bH4qh7ALJOKF2hS7JcjKgHi9v/CGz++1yh+H6kT8zS37Jr5n+iwSr3XbrWAe2507vDvMs7vkCblISn3Lv0FSsx/o4ayrObtdJvJXtnCP8BL9XVwg==">
    <input type="hidden" name="txnType" value="01">
    <input type="hidden" name="channelType" value="07">
    <input type="hidden" name="frontUrl" value="http://shoven.nat123.net:10010/payment/return/unionpay">
    <input type="hidden" name="encoding" value="UTF-8">
    <input type="hidden" name="version" value="5.1.0">
    <input type="hidden" name="accessType" value="0">
    <input type="hidden" name="txnTime" value="201909263044040">
    <input type="hidden" name="merId" value="777290058110048">
    <input type="hidden" name="currencyCode" value="156">
    <input type="hidden" name="txnAmt" value="0.01">
    <input type="hidden" name="signMethod" value="01">
    <input type="hidden" name="riskRateInfo" value="{commodityName=测试商品 iphonexs 256G 黑色}">
    <input type="submit" value="立即支付" style="display:none" >
</form>
<form id ="pay_form" method="post" action="https://gateway.test.95516.com/gateway/api/frontTransReq.do" >
    <input type="hidden" name="bizType" id="bizType" value="000201"/>
    <input type="hidden" name="txnSubType" id="txnSubType" value="01"/>
    <input type="hidden" name="orderId" id="orderId" value="20190920161550"/>
    <input type="hidden" name="backUrl" id="backUrl" value="http://222.222.222.222:8080/ACPSample_B2C/backRcvResponse"/>
    <input type="hidden" name="signature" id="signature" value="fcGHKyLPhsFhti4g8PqR53jsq8Bg/JXjqEtRyB9ATYLCsR5/2skjtHLTXAkmaKnEy3vxaM5pdKNSsCgYYpE9FOrASnWw0TaRoORb67KibnhNYZQQsGB8A3w3wVP1o8SW3c+V1mRKF+p+P1PvjB4YKNkFylwA0BKMg2hDI6N9f+GpK21Ujdiq4/fwEM/GoiGLSmweT4+EwSotulykIzNmETsGz3D4ZM3EMlA46M+/r/WUgJ7Dny9sXaFgya0aqYGs0LKCSXMvAW47Vmo8USek+N1YtHGP44jG3S+kD49Ng6u7jnV/FRLnU2vPG8ifLxKepAQzngR/ewdtmpV0wo03mg=="/>
    <input type="hidden" name="txnType" id="txnType" value="01"/>
    <input type="hidden" name="channelType" id="channelType" value="07"/>
    <input type="hidden" name="frontUrl" id="frontUrl" value="http://localhost:8080/ACPSample_B2C/frontRcvResponse"/>
    <input type="hidden" name="certId" id="certId" value="68759663125"/>
    <input type="hidden" name="encoding" id="encoding" value="UTF-8"/>
    <input type="hidden" name="version" id="version" value="5.1.0"/>
    <input type="hidden" name="accessType" id="accessType" value="0"/>
    <input type="hidden" name="txnTime" id="txnTime" value="20190920161550"/>
    <input type="hidden" name="merId" id="merId" value="777290058110048"/>
    <input type="hidden" name="payTimeout" id="payTimeout" value="20190920165433"/>
    <input type="hidden" name="currencyCode" id="currencyCode" value="156"/>
    <input type="hidden" name="signMethod" id="signMethod" value="01"/>
    <input type="hidden" name="txnAmt" id="txnAmt" value="1000"/>
    <input type="hidden" name="riskRateInfo" id="riskRateInfo" value="{commodityName=测试商品名称}"/></form>
