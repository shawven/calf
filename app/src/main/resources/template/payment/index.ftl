<!DOCTYPE html>
<html lang="zh_CN">
<#include '*/header.ftl'>
<body>
<div class="container-fluid">
    <h2 class="text-center  m-5">收银台</h2>
    <div>
        <ul class="nav nav-tabs" role="tablist" id="myTabs">
            <li role="presentation" class="nav-item">
                <a href="#alipay" class="nav-link px-4" aria-controls="home" role="tab" data-toggle="tab">支付宝</a>
            </li>
            <li role="presentation" class="nav-item">
                <a href="#wechat" class="nav-link px-4" aria-controls="profile" role="tab" data-toggle="tab">微信</a>
            </li>
            <li role="presentation" class="nav-item">
                <a href="#unionpay" class="nav-link px-4" aria-controls="messages" role="tab" data-toggle="tab">银联</a>
            </li>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active p-3" id="alipay">
                <div class="row my-4">
                    <div class="col-md-6">
                        <button class="btn btn-primary" aria-disabled="true">跳转到支付宝</button>
                        <div class="d-none direct"></div>
                    </div>
                    <div class="col-md-6 alipay-iframe">
                    </div>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane" id="wechat">
                <div class="row my-4">
                    <div class="col-md-6">
                        <button class="btn btn-success">跳转到微信</button>
                    </div>
                    <div class="col-md-6 wechat-qrcode">

                    </div>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane" id="unionpay">
                <div class="row my-4">
                    <div class="col-md-6">
                        <button class="btn btn-dark">跳转到银联</button>
                    </div>
                    <div class="col-md-6">

                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#include '*/footer.ftl'>
<script>
    $(function(){
        $('#myTabs li:first-child a').tab('show')
    })

    $('#myTabs a').click(function (e) {
        e.preventDefault()
        $(this).tab('show')
    })

    var timer
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var supplier = e.target.hash
        if (supplier === "#alipay") {
            // getAlipayQrCode()
            console.log("清除定时器")
            clearTimeout(timer)
        }
        if (supplier === "#wechat") {
            // getWechatQrCode()
            // showQrCode(".wechat-qrcode", "weixin://wxpay/s/An4baqw")
            callback("wx20190904103016157592", 1500)

        }
        if (supplier === "#unionpay") {
            console.log("清除定时器")
            clearTimeout(timer)
            // getQrCode("alipay")
        }

    })

    function getAlipayQrCode() {
        var tabPane = document.querySelector("#alipay")

        // 1. 自动构造iframe显示二维码支付
        $.post("${baseUrl}/payment/pay/alipay/web?qrCode=true&width=200&orderId=" + new Date().getTime(), function (result) {
            var formHtml = result.data.form;
            // 构造iframe
            var iframe = document.createElement("iframe")
            iframe.className="border-0"
            iframe.scrolling = "no"
            iframe.width = "200px"
            iframe.height = "200px"
            tabPane.querySelector(".alipay-iframe").innerHTML = ''
            tabPane.querySelector(".alipay-iframe").appendChild(iframe)
            // 在iframe里填充表单
            var iframeBody = iframe.contentWindow.document.getElementsByTagName("body")[0]
            iframeBody.innerHTML = formHtml
            // 自动提交iframe里的表单
            iframeBody.getElementsByTagName("form")[0].submit()
        }, "json").fail(function (result) {
            console.log(result.code + ":" + result.message)
        })

        // 2. 跳转页面支付
        // 在当前页面填充表单
        // 给按钮设置提交提交表单事件
        tabPane.querySelector("button").onclick = function() {
            $.post("${baseUrl}/payment/pay/alipay/web",{orderId:  new Date().getTime()}, function (result) {
                tabPane.querySelector(".direct").innerHTML = result.data.form
                tabPane.querySelector("form").target = "_blank"
                tabPane.querySelector("form").submit()
            }, "json").fail(function (result) {
                console.log(result.code + ":" + result.message)
            })
        }
    }

    function getWechatQrCode() {
        $.post("${baseUrl}/payment/pay/wechat/web", {orderId:  new Date().getTime()}, function (result) {
            var orderId= result.data.orderId
            showQrCode(".wechat-qrcode", result.data.codeUrl)
            callback(orderId, 1500, 5)
        }, "json").fail(function (result) {
            console.log(result.code + ":" + result.message)
        })
    }


    function callback(orderId, timeout) {
        timer = setTimeout(function(){
            console.log("轮训查询支付结果")
            $.get("${baseUrl}/payment/query/wechat", {orderId: orderId}, function () {
                document.querySelector(".wechat-qrcode").innerHTML = "支付成功"
            }, "json").fail(function (result) {
                callback(orderId, timeout)
            })
        }, timeout)
    }

    function showQrCode(selector, url) {
        var el = document.querySelector(selector);
        el.innerHTML = ''
        new QRCode(el, url);
    }



</script>
</body>
</html>
