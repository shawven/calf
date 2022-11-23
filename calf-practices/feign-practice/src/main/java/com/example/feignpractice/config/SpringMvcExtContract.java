package com.example.feignpractice.config;

import feign.MethodMetadata;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class SpringMvcExtContract extends SpringMvcContract {

	private final Environment env;

	public SpringMvcExtContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors,
                                ConversionService conversionService, Environment env) {
		super(annotatedParameterProcessors, conversionService);
		this.env = env;
	}

	@Override
	protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
		super.processAnnotationOnClass(data, clz);
		if (clz.getInterfaces().length == 0) {
			RequestWith req = AnnotatedElementUtils.findMergedAnnotation(clz, RequestWith.class);
			defaultRequestInit(req, data);
		}
	}

	@Override
	protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
		super.processAnnotationOnMethod(data, methodAnnotation, method);
        if (!(methodAnnotation instanceof RequestWith)
                && !methodAnnotation.annotationType().isAnnotationPresent(RequestWith.class)) {
            return;
        }
		RequestWith req = AnnotatedElementUtils.findMergedAnnotation(method, RequestWith.class);
		defaultRequestInit(req, data);
	}

	private void defaultRequestInit(RequestWith req, MethodMetadata data) {
		if (req == null) {
			return;
		}
		Dict[] headers = req.headers();
		Dict[] queries = req.queries();
		if (headers != null && headers.length > 0) {
			for (Dict d : headers) {
				if (d != null && StringUtils.isNotBlank(d.name())) {
					String[] vals = d.value();
					for (int i = 0; i < vals.length; i++) {
						vals[i] = env.resolvePlaceholders(vals[i]);
					}
					data.template().header(env.resolvePlaceholders(d.name()), vals);
				}
			}
		}

		if (queries != null && queries.length > 0) {
			for (Dict d : queries) {
				if (d != null && StringUtils.isNotBlank(d.name())) {
					String[] vals = d.value();
					for (int i = 0; i < vals.length; i++) {
						vals[i] = env.resolvePlaceholders(vals[i]);
					}
					data.template().query(env.resolvePlaceholders(d.name()), vals);
				}
			}
		}

        int connectTimeoutMillis = req.connectTimeoutMillis();
        if (connectTimeoutMillis != 0) {
            data.template().header("$_CONNECT_TIMEOUT_MILLIS", String.valueOf(connectTimeoutMillis));
        }
        int readTimeoutMillis = req.readTimeoutMillis();
        if (readTimeoutMillis != 0) {
            data.template().header("$_READ_TIMEOUT_MILLIS", String.valueOf(readTimeoutMillis));
        }
    }
}
