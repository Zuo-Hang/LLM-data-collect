package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;

/**
 * 灵活数字类型，可以处理空字符串、null 和数字
 */
@Data
@JsonDeserialize(using = FlexibleNumber.Deserializer.class)
public class FlexibleNumber {
    
    private String value;
    
    public FlexibleNumber() {
        this.value = "";
    }
    
    public FlexibleNumber(String value) {
        this.value = value != null ? value : "";
    }
    
    public String string() {
        return value != null ? value : "";
    }
    
    public double toFloat() {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public int toInt() {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }
    
    public Double toPtr() {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            try {
                return (double) Integer.parseInt(value);
            } catch (NumberFormatException e2) {
                return null;
            }
        }
    }
    
    public static class Deserializer extends JsonDeserializer<FlexibleNumber> {
        @Override
        public FlexibleNumber deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String str = p.getText();
            if (str == null || str.trim().isEmpty() || 
                "null".equalsIgnoreCase(str) || "\"\"".equals(str)) {
                return new FlexibleNumber("");
            }
            // 去除引号
            str = str.trim().replaceAll("^\"|\"$", "");
            return new FlexibleNumber(str);
        }
    }
}

