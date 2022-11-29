<!DOCTYPE html>
<html lang="zh_CN">
<#include '*/header.ftl'>
<body>
<div class="container-fluid">
    <h2 class="text-center  m-5">收银台</h2>
    <div>
        <ul class="nav nav-tabs" role="tablist" id="myTabs">
            <li role="presentation" class="nav-attendanceInputDoc">
                <a href="#alipay" class="nav-link px-4" aria-controls="home" role="tab" data-toggle="tab">支付宝</a>
            </li>
            <li role="presentation" class="nav-attendanceInputDoc">
                <a href="#wechat" class="nav-link px-4" aria-controls="profile" role="tab" data-toggle="tab">微信</a>
            </li>
            <li role="presentation" class="nav-attendanceInputDoc">
                <a href="#unionpay" class="nav-link px-4" aria-controls="messages" role="tab" data-toggle="tab">银联</a>
            </li>
            <li role="presentation" class="nav-attendanceInputDoc">
                <a href="#unionpay_b2b" class="nav-link px-4" aria-controls="messages" role="tab" data-toggle="tab">银联(公账)</a>
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
            <div role="tabpanel" class="tab-pane p-3" id="wechat">
                <div class="row my-4">
                    <div class="col-md-6">
                        <button class="btn btn-success">跳转到微信</button>
                    </div>
                    <div class="col-md-6 wechat-qrcode">
                    </div>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane p-3" id="unionpay">
                <div class="row my-4">
                    <div class="col-md-6">
                        <button class="btn btn-dark">跳转到银联</button>
                        <div class="d-none direct"></div>
                    </div>
                    <div class="col-md-6 unionpay-alert">
                    </div>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane p-3" id="unionpay_b2b">
                <div class="row my-4">
                    <div class="col-md-6">
                        <button class="btn btn-dark">跳转到银联(公账)</button>
                        <div class="d-none direct"></div>
                    </div>
                    <div class="col-md-6 unionpay_b2b-alert">
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="alert-success text-center font-weight-bold ">
    </div>
</div>
<#include '*/footer.ftl'>
<script>
    var isPc = true;

    $(function(){
        $('#myTabs li:first-child a').tab('show')
    })

    $('#myTabs a').click(function (e) {
        e.preventDefault()
        $(this).tab('show')
    })

    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var supplier = e.target.hash
        if (supplier === "#alipay") {
            window.clearInterval(timer)
            getAlipayQrCode()
        }
        if (supplier === "#wechat") {
            window.clearInterval(timer)
            getWechatQrCode()
        }
        if (supplier === "#unionpay") {
            window.clearInterval(timer)
            getUnionpay()
        }
        if (supplier === "#unionpay_b2b") {
            window.clearInterval(timer)
            getUnionpayB2B()
        }
    })

    function getAlipayQrCode() {
        var orderId = new Date().getTime();
        var tabPane = document.querySelector("#alipay")

        if (isPc) {
            // 1. 自动构造iframe显示二维码支付
            $.post("${baseUrl}/payment/pay/alipay/web_qrc?size=200&orderId=" + orderId, function (result) {
                query(result.data.orderId, "alipay", function(){
                    showMsg("支付宝支付成功")
                })
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
                $.post("${baseUrl}/payment/pay/alipay/web",{orderId:  orderId}, function (result) {
                    query(result.data.orderId, "alipay", function(){
                        showMsg("支付宝支付成功")
                    })
                    tabPane.querySelector(".direct").innerHTML = result.data.form
                    tabPane.querySelector("form").target = "_blank"
                    tabPane.querySelector("form").submit()
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        } else {

        }

    }

    function getWechatQrCode() {
        var orderId = new Date().getTime();
        if (isPc) {
            $.post("${baseUrl}/payment/pay/wechat/web_qrc", {orderId: orderId}, function (result) {
                showQrCode(".wechat-qrcode", result.data.codeUrl)
                query(result.data.orderId, "wechat", function(){
                    showMsg("微信支付成功")
                })
            }, "json").fail(function (result) {
                console.log(result.code + ":" + result.message)
            })
        } else {
            $.post("${baseUrl}/payment/pay/wechat/wap", {orderId: orderId}, function (result) {
                window.location.href = result.data.url
                query(result.data.orderId, "wechat", function(){
                    showMsg("微信支付成功")
                })
            }, "json").fail(function (result) {
                console.log(result.code + ":" + result.message)
            })
        }
    }

    function getUnionpay() {
        var tabPane = document.querySelector("#unionpay")
        if (isPc) {
            // 2. 跳转页面支付
            // 在当前页面填充表单
            // 给按钮设置提交提交表单事件
            tabPane.querySelector("button").onclick = function() {
                $.post("${baseUrl}/payment/pay/unionpay/web",{orderId: new Date().getTime()}, function (result) {
                    query(result.data.orderId, "unionpay", function(){
                        showMsg("银联支付成功")
                    })
                    tabPane.querySelector(".direct").innerHTML = result.data.form
                    tabPane.querySelector("form").target = "_blank"
                    tabPane.querySelector("form").submit()
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        } else {
            // 2. 跳转页面支付
            // 在当前页面填充表单
            // 给按钮设置提交提交表单事件
            tabPane.querySelector("button").onclick = function() {
                $.post("${baseUrl}/payment/pay/unionpay/wap",{orderId: new Date().getTime()}, function (result) {
                    query(result.data.orderId, "unionpay", function(){
                        showMsg("银联支付成功")
                    })
                    tabPane.querySelector(".direct").innerHTML = result.data.form
                    tabPane.querySelector("form").target = "_blank"
                    tabPane.querySelector("form").submit()
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        }
    }

    function getUnionpayB2B() {
        var tabPane = document.querySelector("#unionpay_b2b")
        if (isPc) {
            // 2. 跳转页面支付
            // 在当前页面填充表单
            // 给按钮设置提交提交表单事件
            tabPane.querySelector("button").onclick = function() {
                $.post("${baseUrl}/payment/pay/unionpay_b2b/web",{orderId: new Date().getTime()}, function (result) {
                    query(result.data.orderId, "unionpay_b2b", function(){
                        showMsg("银联支付成功")
                    })
                    tabPane.querySelector(".direct").innerHTML = result.data.form
                    tabPane.querySelector("form").target = "_blank"
                    tabPane.querySelector("form").submit()
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        } else {
            // 2. 跳转页面支付
            // 在当前页面填充表单
            // 给按钮设置提交提交表单事件
            tabPane.querySelector("button").onclick = function() {
                $.post("${baseUrl}/payment/pay/unionpay_b2b/wap",{orderId: new Date().getTime()}, function (result) {
                    query(result.data.orderId, "unionpay_b2b", function(){

                    })
                    tabPane.querySelector(".direct").innerHTML = result.data.form
                    tabPane.querySelector("form").target = "_blank"
                    tabPane.querySelector("form").submit()
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        }
    }

    var timer
    function query(orderId, payWay, callback) {
        showMsg("")
        window.clearInterval(timer)
        timer = setInterval(function(){
            console.log("轮训查询支付结果")
            $.get("${baseUrl}/payment/query/" + payWay, {orderId: orderId}, function (result) {
                if (result.data.success) {
                    callback()
                    window.clearInterval(timer)
                }
            }, "json").fail(function (result) {
                console.log(result)
            })
        }, 5000)
    }

    function showQrCode(selector, url) {
        var el = document.querySelector(selector);
        el.innerHTML = ''
        new QRCode(el, url);
    }

    function showMsg(msg) {
        document.querySelector(".alert-success").innerHTML = msg
    }


</script>
</body>
</html>
