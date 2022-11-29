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
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active p-3" id="alipay">
                <div class="row my-4">
                    <div class="col-md-6">
                        <input type="text" class="input-medium" id="alipay_code"/>
                        <button class="btn btn-primary">扫码付款</button>
                    </div>
                    <div class="col-md-6 alipay-qrcode">
                    </div>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane  p-3" id="wechat">
                <div class="row my-4">
                    <div class="col-md-6">
                        <input type="text" class="input-medium" id="wechat_code"/>
                        <button class="btn btn-success">扫码付款</button>
                    </div>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane p-3 " id="unionpay">
                <div class="row my-4">
                    <div class="col-md-6">
                        <input type="text" class="input-medium" id="unionpay_code"/>
                        <button class="btn btn-dark">扫码付款</button>
                        <div class="d-none direct"></div>
                    </div>
                    <div class="col-md-6 unionpay-qrcode">
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
            getAlipay()
        }
        if (supplier === "#wechat") {
            window.clearInterval(timer)
            getWechat()
        }
        if (supplier === "#unionpay") {
            window.clearInterval(timer)
            getUnionpay()
        }
    })

    function getAlipay() {
        var orderId = new Date().getTime();
        var tabPane = document.querySelector("#alipay")

        if (isPc) {
            $.post("${baseUrl}/payment/pay/alipay/qrc", {orderId: orderId, storeId: "NJ_001"}, function (result) {
                showQrCode(".alipay-qrcode", result.data.codeUrl)
                query(result.data.orderId, "alipay", function(){
                    showMsg("微信支付成功")
                })
            }, "json").fail(function (result) {
                console.log(result.code + ":" + result.message)
            })

            tabPane.querySelector("button").onclick = function() {
                var authCode = document.querySelector("#alipay_code").value
                $.post("${baseUrl}/payment/pay/alipay/f2f",{authCode: authCode, orderId: orderId}, function (result) {
                    showMsg("支付宝支付成功")
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        } else {

        }

    }

    function getWechat() {
        var orderId = new Date().getTime();
        var tabPane = document.querySelector("#wechat")

        if (isPc) {
            tabPane.querySelector("button").onclick = function() {
                var authCode = document.querySelector("#wechat_code").value
                $.post("${baseUrl}/payment/pay/wechat/f2f",{authCode: authCode, orderId: orderId}, function (result) {
                    showMsg("微信支付成功")
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        } else {

        }
    }

    function getUnionpay() {
        var orderId = new Date().getTime();
        var tabPane = document.querySelector("#unionpay")
        if (isPc) {
            $.post("${baseUrl}/payment/pay/unionpay/qrc", {orderId: orderId, storeId: "NJ_001"}, function (result) {
                showQrCode(".unionpay-qrcode", result.data.codeUrl)
                query(result.data.orderId, "unionpay", function(){
                    showMsg("银联支付成功")
                })
            }, "json").fail(function (result) {
                console.log(result.code + ":" + result.message)
            })

            tabPane.querySelector("button").onclick = function() {
                var authCode = document.querySelector("#unionpay_code").value
                $.post("${baseUrl}/payment/pay/unionpay/f2f",{authCode: authCode, orderId: orderId}, function (result) {
                    showMsg("银联支付成功")
                }, "json").fail(function (result) {
                    console.log(result)
                })
            }
        } else {

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
                    document.querySelector(".alert-success").classList.remove("d-none")
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
