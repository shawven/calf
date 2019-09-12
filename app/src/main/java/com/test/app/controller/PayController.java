package com.test.app.controller;

import com.test.app.common.Response;
import com.test.payment.domain.*;
import com.test.payment.PaymentManager;
import com.test.payment.supplier.PaymentSupplierEnum;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.PrintWriter;
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

    private Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private PaymentManager paymentManager;

    @GetMapping
    public String index() {
        return "payment/index";
    }

    /**
     * @param client 支付宝、微信...
     * @param supplier web、wap...
     * @param orderId 订单号
     * @param qrCode  二维码支持
     *                   PC模式下 支付宝  默认跳转支付宝支付  当 qrCode = true 时 支付宝支持内置二维码
     *                             微信    只支持内置二维码支付
     * @return
     */
    @PostMapping("pay/{supplier}/{client}")
    public ResponseEntity pay(@PathVariable String client, @PathVariable String supplier, String orderId,
                              @RequestParam(required = false) String qrCode,
                              @RequestParam(required = false) String width) {
        PaymentTradeRequest tradeRequest = new PaymentTradeRequest(supplier, client);

        tradeRequest.setPrincipal("13111111111");
        tradeRequest.setSubject("测试商品 iphonexs 256G 黑色");
        tradeRequest.setBody("测试商品 iphonexs 256G 黑色仅售6666元，快速抢购");
        tradeRequest.setOutTradeNo(orderId);
        tradeRequest.setAmount("0.01");
        tradeRequest.setIp("127.0.0.1");
        tradeRequest.putOption("qrCode", qrCode);
        tradeRequest.putOption("width", width);

        PaymentTradeResponse rsp = paymentManager.pay(tradeRequest);

        if (!rsp.isSuccess()) {
            return Response.error(rsp.getErrorMsg());
        }
        rsp.putBody("orderId", orderId);
        return Response.ok(rsp.getBody());
    }

    @PostMapping("notify/{supplier}")
    public void asyncNotify(@NotBlank @PathVariable String supplier, HttpServletRequest request,
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
            processOrder(rsp.getOutTradeNo());
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
            processOrder(rsp.getOutTradeNo());
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
        PaymentTradeQueryResponse rsp = paymentManager.query(tradeQueryRequest);

        if (rsp.isSuccess()) {
            processOrder(rsp.getOutTradeNo());
            return Response.ok(rsp.getBody());
        }

        return Response.error(rsp.getErrorMsg());
    }

    @GetMapping("redirect")
    public String redirect(String supplier, String form, Model model) {
        PaymentSupplierEnum paymentSupplier = PaymentSupplierEnum.valueOf(supplier.toUpperCase());
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

    private void processOrder(String orderId) {
        logger.warn("正在处理订单:[" + orderId + "]，必须先判断订单状态");
    }
}
