<!DOCTYPE html>
<html lang="zh_CN">
<#include '*/header.ftl'>
<body>
<div class="container-fluid">
    <h1 class="text-center">统一二维码</h1>
    <div id="qrcode"></div>
</div>
<#include '*/footer.ftl'>
<script>
    $.get("${baseUrl}/payment/qrcode", function (result) {
        getQrcode(result.data.qrcode)
    }, "json").fail(function (result) {

    })


    var timer
    function query(orderId, payWay, callback) {
        window.clearInterval(timer)
        timer = setInterval(function(){
            $.get("${baseUrl}/payment/query/" + payWay, {orderId: orderId}, function () {
                callback()
            }, "json").fail(function (result) {

            })
        }, 3000)
    }

    // query(getQueryVariable("s") , "alipay", function(){
    //     alert("alipay支付成功")
    // })

    function getQrcode(text) {
        $('#qrcode').qrcode({
            render: 'canvas', // render method: 'canvas', 'image' or 'div' 渲染模式 三种，因为我需要生成图片，以便用户下载，选的'image'
            ecLevel: 'H', // error correction level: 'L', 'M', 'Q' or 'H' 识别度 H最高
            size: 400, // size in pixel  画布大小
            fill: '#000',  // code color or image element
            background: '#fff', // background color or image element, null for transparent background
            text: text, // content
            radius: 0 , // corner radius relative to module width: 0.0 .. 0.5
            quiet: 0, // quiet zone in modules  白边的块数
            mode: 2,  // modes / 0: normal / 1: label strip / 2: label box / 3: image strip / 4: image box
            // 5种模式： 0是普通 / 1是标语占中间一行 / 2标语占中间一块 / 3图片站中间一行 / 4图片占中间一块 较常用的是4
            label: 'wqb',
        });
    }

    function getQueryVariable(variable) {
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i=0;i<vars.length;i++) {
            var pair = vars[i].split("=");
            if(pair[0] === variable){return pair[1];}
        }
        return "";
    }
</script>
</body>
</html>
