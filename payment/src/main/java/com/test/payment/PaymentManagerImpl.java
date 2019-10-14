package com.test.payment;

import com.test.payment.client.PaymentClientTypeEnum;
import com.test.payment.domain.*;
import com.test.payment.properties.GlobalProperties;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.HttpUtil;
import com.test.payment.support.PaymentContextHolder;
import com.test.payment.support.PaymentLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.test.payment.support.PaymentUtils.isBlankString;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public class PaymentManagerImpl implements PaymentManager {

    private PaymentLogger logger = PaymentLogger.getLogger(PaymentManagerImpl.class);

    private Map<PaymentSupplierEnum, Map<PaymentClientTypeEnum, PaymentOperations>> suppliers;

    public PaymentManagerImpl(List<PaymentOperations> suppliers, GlobalProperties globalProperties) {
        this.suppliers = index(suppliers);
        init(globalProperties);
    }

    private void init(GlobalProperties globalProperties) {
        // 配置http工具
        HttpUtil httpUtil = HttpUtil.builder()
                .setConnectTimeout(globalProperties.getConnectTimeout())
                .setReadTimeout(globalProperties.getReadTimeout())
                .setMaxTotal(globalProperties.getMaxTotal())
                .setMaxPerRoute(globalProperties.getMaxPerRoute())
                .setConnectionTimeToLive(globalProperties.getConnectionTimeToLive())
                .build();

        PaymentContextHolder.setHttp(httpUtil);
        PaymentContextHolder.setGlobalProperties(globalProperties);
        CurrencyTools.setUnitOfCents(globalProperties.getCurrencyCents());
    }

    @Override
    public Set<PaymentSupplierEnum> listAvailableSuppliers(PaymentClientTypeEnum paymentClient) {
        return suppliers.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(paymentClient))
                .flatMap(entry -> Stream.of(entry.getKey()))
                .collect(toSet());
    }

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        if (isBlankString(request.getOutTradeNo())) {
            throw new IllegalArgumentException("支付商户交易号不能为空");
        }
        if (isBlankString(request.getSubject())) {
            throw new IllegalArgumentException("支付商品主题不能为空");
        }
        if (isBlankString(request.getAmount())) {
            throw new IllegalArgumentException("订单金额为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeResponse response;
        try {
            response = paymentOperations.pay(request);
            if (logger.isDebugEnabled()) {
                logger.debug(request, "支付结果：[{}]", response);
            }
        } catch (UnsupportedOperationException e) {
            response = new PaymentTradeResponse();
            logger.warn(request, "尚未支持");
        }
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        if (isBlankString(request.getOutTradeNo())) {
            throw new IllegalArgumentException("查询支付商户交易号不能为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeQueryResponse response;
        try {
            response = paymentOperations.query(request);
            if (logger.isDebugEnabled()) {
                logger.debug(request, "查询支付交易结果：[{}]", response);
            }
        } catch (UnsupportedOperationException e) {
            response = new PaymentTradeQueryResponse();
            logger.warn(request, "尚未支持");
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeCallbackResponse response;
        try {
            response = paymentOperations.syncReturn(request);
            if (logger.isDebugEnabled()) {
                logger.debug(request, "同步跳转结果：[{}]", response);
            }
        } catch (UnsupportedOperationException e) {
            response = new PaymentTradeCallbackResponse();
            logger.warn(request, "尚未支持");
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeCallbackResponse response;
        try {
            response = paymentOperations.asyncNotify(request);
            if (logger.isDebugEnabled()) {
                logger.debug(request, "异步回调结果：[{}]", response);
            }
        } catch (UnsupportedOperationException e) {
            response = new PaymentTradeCallbackResponse();
            logger.warn(request, "尚未支持");
        }
        return response;
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        if (isBlankString(request.getOutRefundNo())) {
            throw new IllegalArgumentException("申请退款商户退款号不能为空");
        }
        if (isBlankString(request.getOutTradeNo())) {
            throw new IllegalArgumentException("申请退款商户交易号不能为空");
        }
        if (isBlankString(request.getTradeNo())) {
            throw new IllegalArgumentException("申请退款平台交易号不能为空");
        }
        if (isBlankString(request.getRefundAmount())) {
            throw new IllegalArgumentException("申请退款金额为空");
        }
        if (isBlankString(request.getTotalAmount())) {
            throw new IllegalArgumentException("申请退款订单总金额为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeRefundResponse response;
        try {
            response = paymentOperations.refund(request);
            if (logger.isDebugEnabled()) {
                logger.debug(request, "申请退款结果：[{}]", response);
            }
        } catch (UnsupportedOperationException e) {
            response = new PaymentTradeRefundResponse();
            logger.warn(request, "尚未支持");
        }

        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        if (isBlankString(request.getTradeNo())) {
            throw new IllegalArgumentException("查询退款原平台交易号不能为空");
        }
        if (isBlankString(request.getOutRefundNo())) {
            throw new IllegalArgumentException("查询退款商户退款号不能为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeRefundQueryResponse response;
        try {
            response = paymentOperations.refundQuery(request);
            if (logger.isDebugEnabled()) {
                logger.debug(request, "查询退款结果：[{}]", response);
            }
        } catch (UnsupportedOperationException e) {
            response = new PaymentTradeRefundQueryResponse();
            logger.warn(request, "尚未支持");
        }

        return response;
    }

    /**
     * 获取支付提供者
     *
     * @param paymentRequest
     * @return
     */
    private PaymentOperations getProvider(PaymentRequest paymentRequest) {
        PaymentSupplierEnum supplier = paymentRequest.getPaymentSupplier();
        PaymentClientTypeEnum clientType = paymentRequest.getPaymentClientType();
        Map<PaymentClientTypeEnum, PaymentOperations> clientTypes = this.suppliers.get(supplier);

        if (suppliers == null || suppliers.isEmpty()) {
            throw new PaymentException("未配置[" + supplier.getName() + "]供应商");
        }

        // 未指定客户端类型或者为空返回第一个即可
        if (clientType == PaymentClientTypeEnum.NONE || clientType == null) {
            return clientTypes.values().iterator().next();
        } else {
            PaymentOperations paymentOperations = clientTypes.get(clientType);
            if (paymentOperations == null) {
                throw new PaymentException("当前环境不支持[" + clientType.getName() + "]支付方式");
            }
            return paymentOperations;
        }
    }

    /**
     * 索引化提供者
     *
     * @param operationsList
     * @return
     */
    private Map<PaymentSupplierEnum, Map<PaymentClientTypeEnum, PaymentOperations>>
    index(List<PaymentOperations> operationsList) {
        if (operationsList == null) {
            return emptyMap();
        }

        Map<PaymentSupplierEnum, Map<PaymentClientTypeEnum, PaymentOperations>> suppliers = new HashMap<>();
        operationsList.stream()
                .filter(operations -> {
                    PaymentSupplierEnum supplier = operations.getSupplier();
                    PaymentClientTypeEnum client = operations.getClientType();
                    String className = operations.getClass().getName();
                    if (supplier == null) {
                        logger.rawWarn("加载{}失败，支付供应商未配置", className);
                        return false;
                    }
                    if (client == null) {
                        logger.rawWarn("加载{}失败，客户端类型未配置", className);
                        return false;
                    }
                    return true;
                })
                .collect(groupingBy(PaymentOperations::getSupplier, toSet()))
                .forEach((supplier, operationsSet) -> {
                    StringBuilder loaded = new StringBuilder();
                    for (PaymentOperations operations : operationsSet) {
                        PaymentClientTypeEnum client = operations.getClientType();
                        Map<PaymentClientTypeEnum, PaymentOperations> clientTypes = suppliers.get(supplier);

                        if (clientTypes == null) {
                            clientTypes = new HashMap<>(8);
                            clientTypes.put(client, operations);
                            suppliers.put(supplier, clientTypes);
                        } else {
                            clientTypes.put(client, operations);
                        }
                        loaded.append(client.getName()).append(",");
                    }
                    if (loaded.length() > 0) {
                        loaded.deleteCharAt(loaded.length() - 1);
                        logger.rawInfo("加载{}[{}]支付方式成功", supplier.getName(), loaded);
                    }
                });
        return suppliers;
    }
}
