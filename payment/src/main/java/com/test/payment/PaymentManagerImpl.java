package com.test.payment;

import com.test.payment.client.PaymentClientTypeEnum;
import com.test.payment.domain.*;
import com.test.payment.properties.PaymentProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.PaymentLogger;
import com.test.payment.support.PaymentUtils;

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

    public PaymentManagerImpl(List<PaymentOperations> suppliers, PaymentProperties paymentProperties) {
        this.suppliers = index(suppliers, paymentProperties);
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
            throw new IllegalArgumentException("预支付商户交易号不能为空");
        }
        if (isBlankString(request.getSubject())) {
            throw new IllegalArgumentException("预支付商品主题不能为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeResponse response = paymentOperations.pay(request);
        logger.debug(request, "预支付结果：[{}]", response);
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        if (isBlankString(request.getOutTradeNo())) {
            throw new IllegalArgumentException("查询支付商户交易号不能为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeQueryResponse response = paymentOperations.query(request);
        logger.debug(request, "查询支付交易结果：[{}]", response);
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeCallbackResponse response = paymentOperations.syncReturn(request);
        logger.debug(request,  "同步跳转结果：[{}]", response);
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeCallbackResponse response = paymentOperations.asyncNotify(request);
        logger.debug(request,  "异步回调结果：[{}]", response);
        return response;
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        if (isBlankString(request.getOutRefundNo())) {
            throw new IllegalArgumentException("商户退款号不能为空");
        }
        if (isBlankString(request.getOutTradeNo()) && isBlankString(request.getTradeNo())) {
            throw new IllegalArgumentException("商户交易号和平台交易号不能同时为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeRefundResponse response = paymentOperations.refund(request);
        logger.debug(request,  "交易退款结果：[{}]", response);
        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse queryRefund(PaymentTradeRefundQueryRequest request) {
        if (isBlankString(request.getOutRefundNo())) {
            throw new IllegalArgumentException("商户退款号不能为空");
        }
        PaymentOperations paymentOperations = getProvider(request);
        PaymentTradeRefundQueryResponse response = paymentOperations.queryRefund(request);
        logger.debug(request,  "查询交易退款结果：[{}]", response);
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

        // 未指定客户端类型或者为空返回任意一个即可
        if (clientType == PaymentClientTypeEnum.UNIFIED || clientType == null) {
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
     * @param paymentProperties
     * @return
     */
    private Map<PaymentSupplierEnum, Map<PaymentClientTypeEnum, PaymentOperations>>
    index(List<PaymentOperations> operationsList, PaymentProperties paymentProperties) {
        CurrencyTools.setUnitOfCents(paymentProperties.getCurrencyCents());

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
                            if (operations instanceof AbstractPaymentTemplate) {
                                AbstractPaymentTemplate template = (AbstractPaymentTemplate) operations;
                                template.setPaymentProperties(paymentProperties);
                            }
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
