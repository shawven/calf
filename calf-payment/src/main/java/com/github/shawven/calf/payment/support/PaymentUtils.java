package com.github.shawven.calf.payment.support;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public class PaymentUtils {

    public static Map<String, String> parseParameterMap(Map<String, ?> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        for (String name : parameterMap.keySet()) {
            Object value = parameterMap.get(name);
            String valueStr = "";
            if (value instanceof String[]) {
                String[] values = (String[]) value;
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
            } else {
                valueStr = value != null ? value.toString() : "";
            }
            params.put(name, valueStr);
        }
        return params;
    }

    public static String read(InputStream inputStream) {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        char[] buf = new char[4096];
        int n;
        try {
            while (-1 != (n = reader.read(buf))) {
                writer.write(buf, 0, n);
            }
        } catch (IOException e) {
            return null;
        }
        return writer.toString();
    }

    public static byte[] readBytes(InputStream inputStream) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        try {
            while (-1 != (n = inputStream.read(buf))) {
                output.write(buf, 0, n);
            }
        } catch (IOException e) {
            return new byte[]{};
        }
        return output.toByteArray();
    }

    public static Map<String, String> splitPairString(String pairString) {
        if (pairString == null || pairString.length() == 0) {
            return emptyMap();
        }
        String[] keyValuePairs = pairString.split("&");
        if (keyValuePairs.length == 0) {
            return emptyMap();
        }
        LinkedHashMap<String, String> newPairs = new LinkedHashMap<>();
        for (String keyValuePair : keyValuePairs) {
            String[] elements = keyValuePair.split("=", 2);
            if (elements.length != 2) {
                continue;
            }
            try {
                String element = elements[1];
                // 不符合规范的字符预转换decode容错
                element = element.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                element = element.replaceAll("\\+", "%2B");
                String decode = URLDecoder.decode(element, "UTF-8");
                newPairs.put(elements[0], decode);
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return newPairs;
    }

    public static String toPairString(Map<String, ?> params) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int index = 0;
        for (String key : keys) {
            Object value = params.get(key);
            if (!isBlankString(key) && value != null) {
                content.append(index == 0 ? "" : "&").append(key).append("=").append(value);
                ++index;
            }
        }
        return content.toString();
    }

    public static String buildForm(String baseUrl, Map<String, String> parameters) {
        StringBuilder form = new StringBuilder();
        form.append("<form name=\"punchout_form\" method=\"post\" action=\"");
        form.append(baseUrl);
        form.append("\">\n");
        form.append(buildHiddenFields(parameters));
        form.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >\n");
        form.append("</form>\n");
        form.append("<script>document.forms[0].submit();</script>");
        return form.toString();
    }

    private static String buildHiddenFields(Map<String, String> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            StringBuilder hiddenInput = new StringBuilder();
            Set<String> keys = parameters.keySet();
            for (String key : keys) {
                String value = parameters.get(key);
                if (key != null && value != null) {
                    hiddenInput.append("<input type=\"hidden\" name=\"");
                    hiddenInput.append(key);
                    hiddenInput.append("\" value=\"");
                    hiddenInput.append(value.replace("\"", "&quot;")).append("\">\n");
                }
            }
            return hiddenInput.toString();
        } else {
            return "";
        }
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return obj.toString();
        }

        Class<?> cls = obj.getClass();
        StringBuilder builder = new StringBuilder()
                .append(cls.getSimpleName()).append("{");

        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(cls, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        PropertyDescriptor[] all = info.getPropertyDescriptors();
        try {
            for (PropertyDescriptor pd : all) {
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    Object result = readMethod.invoke(obj);
                    builder.append(pd.getName()).append(" = ").append(result).append(",");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
        } catch (Exception ignored) {
        }

        builder.append("}");
        return builder.toString();
    }

    public static boolean isBlankString(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public abstract static class ScheduledTask implements Runnable {

        private Future<?> future;

        private long timeout;

        /**
         * 取消任务
         *
         */
        protected void cancel() {
            if (!future.isCancelled()) {
                future.cancel(false);
            }
        }

        /**
         * 等待任务执行完毕
         */
        public void await() {
            if (future.isCancelled()) {
                return;
            }
            try {
                future.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            } finally {
                if (!future.isCancelled()) {
                    future.cancel(false);
                }
            }
        }

        private void setFuture(Future<?> future) {
            this.future = future;
        }

        private void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * 超时时间内周期性执行任务
     *
     * @param command 执行命令
     * @param period  周期性时间
     * @param timeout 超时时间
     * @return 执行命令
     */
    public static ScheduledTask runningUntilSuccess(ScheduledTask command, int period, int timeout) {
        ScheduledExecutorService executor = ScheduledExecutor.getInstance();
        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(command, 0, period, TimeUnit.SECONDS);
        command.setTimeout(timeout);
        command.setFuture(future);
        return command;
    }

    /**
     * 执行可重试的任务，直到成功或者最大重试次数
     *
     * @param command       执行命令 command返回true 停止重试
     * @param retryConsumer 重试消费者
     * @param retryTimes    重试次数 -1 无限次
     * @return 成功与否
     */
    public static boolean runningUntilSuccess(Callable<Boolean> command, Consumer<Integer> retryConsumer, int retryTimes) {
        boolean infinite = retryTimes < 0;
        int i = 0;
        boolean success;
        do {
            if (i > 0) {
                retryConsumer.accept(i);
            }
            try {
                success = command.call();
            } catch (Exception e) {
                success = false;
            }
        } while (!success && (infinite || ++i < retryTimes));
        return success;
    }

    public static class ScheduledExecutor {

        private static ScheduledExecutorService instance =
                Executors.newScheduledThreadPool(1, Executors.defaultThreadFactory());

        public static ScheduledExecutorService getInstance() {
            return instance;
        }
    }
}
