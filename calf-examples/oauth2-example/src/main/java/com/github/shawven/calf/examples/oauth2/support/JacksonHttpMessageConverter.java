package com.github.shawven.calf.examples.oauth2.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


/**
 * 使用官方自带的json格式类库，fastjson因为content type问题时不时控制台报错、无法直接返回二进制等问题
 * @author kingdee
 */
public class JacksonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public static JsonSerializer<Object> nullArrayJsonSerializer = new JsonSerializer<Object>() {

        @Override
        public void serialize(Object value, JsonGenerator jgen,
                              SerializerProvider provider) throws IOException, JsonProcessingException {
            if (value == null) {
                jgen.writeStartArray();
                jgen.writeEndArray();
            }
        }
    };

    public static JsonSerializer<Object> nullStringJsonSerializer = new JsonSerializer<Object>() {

        @Override
        public void serialize(Object o, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(StringUtils.EMPTY);
        }
    };

    public static JsonSerializer<Object> nullBooleanJsonSerializer = new JsonSerializer<Object>() {

        @Override
        public void serialize(Object o, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeBoolean(false);
        }
    };

    public static class MyBeanSerializerModifier extends BeanSerializerModifier {

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                         BeanDescription beanDesc,
                                                         List<BeanPropertyWriter> beanProperties) {
            for (Object beanProperty : beanProperties) {
                BeanPropertyWriter writer = (BeanPropertyWriter) beanProperty;
                Class<?> clazz = writer.getType().getRawClass();
                if (isArrayType(clazz)) {
                    writer.assignNullSerializer(nullArrayJsonSerializer);
                } else if (isBooleanType(clazz)) {
                    writer.assignNullSerializer(nullBooleanJsonSerializer);
                } else if (isStringType(clazz)) {
                    writer.assignNullSerializer(nullStringJsonSerializer);
                }
            }
            return beanProperties;
        }

        private boolean isArrayType(Class<?> clazz) {
            return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
        }

        private boolean isStringType(Class<?> clazz) {
            return CharSequence.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz);
        }

        private boolean isBooleanType(Class<?> clazz) {
            return clazz.equals(Boolean.class);
        }

    }

    public JacksonHttpMessageConverter() {
        ObjectMapper objectMapper = getObjectMapper();

        SerializerFactory serializerFactory = objectMapper.getSerializerFactory()
                .withSerializerModifier(new MyBeanSerializerModifier());

        objectMapper.setSerializerFactory(serializerFactory);
    }

}
