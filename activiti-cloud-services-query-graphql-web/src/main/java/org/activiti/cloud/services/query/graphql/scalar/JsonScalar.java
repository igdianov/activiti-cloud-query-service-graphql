package org.activiti.cloud.services.query.graphql.scalar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;

public class JsonScalar implements Coercing<Object, Object> {

    @Override
    public Object serialize(Object dataFetcherResult) {
        return dataFetcherResult;
    }

    @Override
    public Object parseValue(Object input) {
        return input;
    }

    @Override
    public Object parseLiteral(Object input) {
        return parseFieldValue((Value) input,  Collections.emptyMap());
    }

    //recursively parse the input into a Map
    private Object parseFieldValue(Object value, Map<String, Object> variables) {
        if (!(value instanceof Value)) {
            throw new IllegalArgumentException(
                    "Expected AST type 'StringValue' but was '" + value + "'."
            );
        }    	
    	
        if (value instanceof StringValue) {
            return ((StringValue) value).getValue();
        }
        if (value instanceof IntValue) {
            return ((IntValue) value).getValue();
        }
        if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue();
        }
        if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue();
        }
        if (value instanceof EnumValue) {
            return ((EnumValue) value).getName();
        }
        if (value instanceof NullValue) {
            return null;
        }
        if (value instanceof ArrayValue) {
        	List<Value> values = ((ArrayValue) value).getValues();
            return values.stream()
                    .map(v -> parseFieldValue(v, variables))
                    .collect(Collectors.toList());
        }
        if (value instanceof ObjectValue) {
        	List<ObjectField> values = ((ObjectValue) value).getObjectFields();
            Map<String, Object> parsedValues = new LinkedHashMap<>();
            
            values.forEach(field -> {
                Object parsedValue = parseFieldValue(field.getValue(), variables);
                parsedValues.put(field.getName(), parsedValue);
            });
            return parsedValues;
        }
        
        //Should never happen, as it would mean the variable was not replaced by the parser
        throw new IllegalArgumentException("Unsupported scalar value type: " + value.getClass().getName());
    }

}
