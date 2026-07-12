package ru.ifmo.person.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
    
    private final ObjectMapper objectMapper;
    
    public JacksonConfig() {
        objectMapper = new ObjectMapper();
        
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        objectMapper.coercionConfigFor(LogicalType.Enum)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        
        objectMapper.coercionConfigFor(LogicalType.Integer)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        
        objectMapper.coercionConfigFor(LogicalType.Float)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        
        objectMapper.coercionConfigFor(LogicalType.DateTime)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
    }
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}

