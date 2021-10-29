package com.github.shawven.calf.examples.pay.controller;

import com.github.shawven.calf.examples.pay.support.Response;
import com.github.shawven.calf.payment.domain.*;
import com.google.common.collect.ImmutableMap;
import com.github.shawven.calf.payment.PaymentManager;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * @author Shoven
 * @date 2019-08-28
 */
@Controller
@RequestMapping("payment")
public class PayController {

    private Map<String, String> refundMap = new ConcurrentHashMap<>();
    private Map<String, String> orderMap = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private PaymentManager paymentManager;

    @GetMapping
    public String index() {
        return "payment/index";
    }

    @GetMapping("index2")
    public String index2() {
        return "payment/index2";
    }

    @GetMapping("index3")
    public String index3() {
        return "payment/index3";
    }

    @GetMapping("unifiedQRC")
    public ResponseEntity getQrCode() {
        String s = RandomStringUtils.randomNumeric(32);
        return Response.ok(ImmutableMap.of("qrcode", "http://shoven.nat123.net:10010/payment/payer/" + s));
    }

    @GetMapping("unifiedQRC/{orderId}")
    public String unifiedPay(@PathVariable String orderId, Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("title", "购买商品");
        return "unifiedQRC";
    }

    /**
     * @param client 支付宝、微信...
     * @param supplier web、wap...
     * @param orderId 订单号
     * @param
     * @return
     */
    @PostMapping("pay/{supplier}/{client}")
    public ResponseEntity pay(@PathVariable String client, @PathVariable String supplier, String orderId,
                              HttpServletRequest request) {
        PaymentTradeRequest tradeRequest = new PaymentTradeRequest(supplier, client, request.getParameterMap());

        tradeRequest.setPrincipal("13111111111");
        tradeRequest.setSubject("测试商品 iphonexs 256G 黑色");
        tradeRequest.setBody("测试商品 iphonexs 256G 黑色仅售6666元，快速抢购");
        tradeRequest.setOutTradeNo(orderId);
        tradeRequest.setAmount("0.01");
        tradeRequest.setIp("127.0.0.1");

        PaymentTradeResponse rsp = paymentManager.pay(tradeRequest);

        if (rsp.isSuccess()) {
            // 面对面付款码支付这种会立即交易完成
            if (rsp.isTradeSuccess()) {
                processOrder(rsp.getOutTradeNo(), rsp.getTradeNo());
                return Response.ok("交易完成");
            }
            rsp.putBody("orderId", orderId);
            return Response.ok(rsp.getBody());
        }
        return Response.error(rsp.getErrorMsg());
    }

    @PostMapping("notify/{supplier}")
    public void asyncNotify(@NotBlank @PathVariable String supplier,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        ServletInputStream inputStream;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        PaymentTradeCallbackRequest callbackRequest = new PaymentTradeCallbackRequest(supplier,
                request.getParameterMap(), inputStream);
        callbackRequest.setPrincipal("13111111111");
        PaymentTradeCallbackResponse rsp = paymentManager.asyncNotify(callbackRequest);

        if (rsp.isSuccess()) {
            processOrder(rsp.getOutTradeNo(), rsp.getTradeNo());
        }

        try {
            // 直接将完整的表单html输出到页面
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(rsp.getReplayMessage());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    @RequestMapping(value = "return/{supplier}", method = {GET, POST})
    public String syncReturn(@NotBlank @PathVariable String supplier, HttpServletRequest request, Model model) {
        ServletInputStream inputStream;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("message", "获取输入参数失败");
            return "redirect:pay/fail";
        }
        PaymentTradeCallbackRequest callbackRequest = new PaymentTradeCallbackRequest(supplier,
                request.getParameterMap(), inputStream);
        callbackRequest.setPrincipal("13111111111");
        PaymentTradeCallbackResponse rsp = paymentManager.syncReturn(callbackRequest);

        if (rsp.isSuccess()) {
            processOrder(rsp.getOutTradeNo(), rsp.getTradeNo());
            return success();
        }
        model.addAttribute("message", rsp.getErrorMsg());
        return fail();
    }

    @GetMapping("query/{supplier}")
    public ResponseEntity query(@NotBlank @PathVariable String supplier, @NotBlank String orderId) {
        PaymentTradeQueryRequest tradeQueryRequest = new PaymentTradeQueryRequest(supplier);

        tradeQueryRequest.setPrincipal("13111111111");
        tradeQueryRequest.setOutTradeNo(orderId);
        tradeQueryRequest.setTradeNo(orderMap.get(orderId));
        PaymentTradeQueryResponse rsp = paymentManager.query(tradeQueryRequest);

        if (rsp.isSuccess()) {
            processOrder(rsp.getOutTradeNo(), rsp.getTradeNo());
            return Response.ok(ImmutableMap.of("success", true));
        }

        return Response.ok(ImmutableMap.of("success", false));
    }

    @PostMapping("refund/{supplier}")
    public ResponseEntity refund(@NotBlank @PathVariable String supplier, @NotBlank String orderId) {
        PaymentTradeRefundRequest refundRequest = new PaymentTradeRefundRequest(supplier);
        String refundNo = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + RandomStringUtils.randomNumeric(5);
        logger.info("退款单号[{}]", refundNo);

        refundMap.put(orderId, refundNo);

        refundRequest.setPrincipal("13111111111");
        refundRequest.setOutTradeNo(orderId);
        refundRequest.setOutRefundNo(refundNo);
        refundRequest.setTradeNo(orderMap.get(orderId));
        refundRequest.setRefundAmount("0.01");
        refundRequest.setTotalAmount("0.01");
        refundRequest.setRefundReason("无条件退款");
        PaymentTradeRefundResponse rsp = paymentManager.refund(refundRequest);

        if (rsp.isSuccess()) {
            processRefundOrder(rsp.getOutTradeNo());
            return Response.ok(rsp.getBody());
        }

        return Response.error(rsp.getErrorMsg());
    }

    @GetMapping("refund/query/{supplier}")
    public ResponseEntity refundQuery(@NotBlank @PathVariable String supplier, @NotBlank String orderId) {
        PaymentTradeRefundQueryRequest refundQueryRequest = new PaymentTradeRefundQueryRequest(supplier);

        refundQueryRequest.setPrincipal("13111111111");
        refundQueryRequest.setOutTradeNo(orderId);
        refundQueryRequest.setTradeNo(orderMap.get(orderId));
        refundQueryRequest.setOutRefundNo(refundMap.get(orderId));
        PaymentTradeRefundQueryResponse rsp = paymentManager.refundQuery(refundQueryRequest);

        if (rsp.isSuccess()) {
            processOrder(rsp.getOutTradeNo(), rsp.getTradeNo());
            return Response.ok(rsp.getBody());
        }

        return Response.error(rsp.getErrorMsg());
    }

    @GetMapping("redirect")
    public String redirect(String supplier, String form, Model model) {
        PaymentProviderEnum paymentSupplier = PaymentProviderEnum.valueOf(supplier.toUpperCase());
        model.addAttribute("supplier", paymentSupplier);
        model.addAttribute("form", form);
        return "payment/redirect";
    }

    @GetMapping("success")
    public String success() {
        return "payment/success";
    }

    @RequestMapping("fail")
    public String fail() {
        return "payment/fail";
    }

    private void processOrder(String orderId, String tradeNo) {
        orderMap.put(orderId, tradeNo);
        logger.warn("正在处理订单:[" + orderId + "]，必须先判断订单状态");
    }

    private void processRefundOrder(String orderId) {
        logger.warn("正在处理退款订单:[" + orderId + "]，必须先判断订单状态");
    }
}
