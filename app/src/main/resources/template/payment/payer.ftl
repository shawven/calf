<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
    <!-- 最新版本的 Bootstrap 核心 CSS 文件 -->
</head>
<#assign baseUrl=springMacroRequestContext.getContextPath() >
<body>
<div style="display: none">
    <div id="alipay">
    </div>
</div>
<script src="/statics/js/kjua.min.js"></script>
<script>
    var payWay = "wechat"
    var orderId = "${orderId}"
    var ua = window.navigator.userAgent
    // if (/MicroMessenger/.test(ua)) {
    //     payWay = "wechat"
    // } else if (/AlipayClient/.test(ua)) {
    //     payWay = "alipay"
    // }
    switch (payWay) {
        case "alipay":
            alipay()
            break
        case "wechat":
            wechatJsApi()
            break;
        case "unionpay":
            unionpay()
            break;
        default:
            alert("请用微信、支付扫码")

    }

    function alipay() {
        var tabPane = document.querySelector("#alipay")
        post("${baseUrl}/payment/pay/alipay/wap", {orderId: orderId}, function (result) {
            tabPane.innerHTML = result.data.form
            tabPane.querySelector("form").submit()
        }, function (result) {
            console.log(result)
        })
    }

    function wechat() {
        post("${baseUrl}/payment/pay/wechat/wap", {orderId: orderId}, function (result) {
            window.location.href=result.data.url
        }, function (result) {
            console.log(result)
        })
    }

    function wechatJsApi() {
        // 获取openId
        var code = getQueryVariable("code");
        if(!code){
            window.location.href = 'https://open.weixin.qq.com/connect/oauth2/authorize' +
                '?appid=wx6b9ebd5d4e720f5f' +
                '&redirect_uri=' + encodeURI(window.location.href) +
                '&response_type=code&scope=snsapi_base&state=123#wechat_redirect';
        }

        if (typeof WeixinJSBridge == "undefined") {
            if (document.addEventListener) {
                document.addEventListener('WeixinJSBridgeReady', jsApiCall, false);
            } else if (document.attachEvent) {
                document.attachEvent('WeixinJSBridgeReady', jsApiCall);
                document.attachEvent('onWeixinJSBridgeReady', jsApiCall);
            }
        } else {
            jsApiCall();
        }

        function jsApiCall() {
            post("${baseUrl}/payment/pay/wechat/jsapi", {orderId: orderId, code: code}, function (result) {
                WeixinJSBridge.invoke(
                    'getBrandWCPayRequest',
                    result.data,
                    function (res) {
                        if(res.err_msg === "get_brand_wcpay_request:ok" ){
                            // 使用以上方式判断前端返回,微信团队郑重提示：
                            //res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
                            window.location.href="${baseUrl}/payment/return/wechat?"+query
                        }
                        WeixinJSBridge.log(res.err_msg);
                        alert(res.err_code + res.err_desc + res.err_msg);
                    }
                );
            }, function (result) {
                console.log(result)
            })

        }
    }

    function unionpay() {
        var tabPane = document.querySelector("#alipay")
        post("${baseUrl}/payment/pay/unionpay/wap", {orderId: orderId}, function (result) {
            tabPane.innerHTML = result.data.form
            tabPane.querySelector("form").submit()
        }, function (result) {
            console.log(result)
        })
    }

    function post(url, data, success, fail) {
        var formData = new FormData();
        for (var key in data) {
            formData.append(key, data[key])
        }
        var xhr = new XMLHttpRequest();
        xhr.open("POST", url, true)
        xhr.send(formData)
        xhr.onreadystatechange = function () {
            var rsp = JSON.parse(xhr.responseText);
            if (xhr.readyState === 4 && xhr.status === 200) {
                success(rsp)
            } else {
                fail(rsp)
            }
        }
    }

    function get(url, data, success, fail) {
        var query = ''
        var i = 0
        for(var key in data) {
            query += i === 0 ? "?" : "&" + key + "=" + data[key]
        }
        url += query;

        var xhr = new XMLHttpRequest();
        xhr.open("get", url, true)
        xhr.onreadystatechange = function () {
            var rsp = JSON.parse(xhr.responseText);
            if (xhr.readyState === 4 && xhr.status === 200) {
                success(rsp)
            } else {
                fail(rsp)
            }
        }
    }

    function getQueryVariable(variable) {
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i = 0; i< vars.length; i++) {
            var pair = vars[i].split("=");
            if( pair[0] === variable) {
                return pair[1];
            }
        }
        return "";
    }
</script>
</body>
</html>
