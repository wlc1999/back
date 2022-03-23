package com.xxxx.server.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 全局日期转换
 */
@Component
public class DataConverter implements Converter<String, LocalDate> /*Converter是全局转换，将String类型转换为LocalDate*/{

    @Override
    public LocalDate convert(String s) {
        try {
            return LocalDate.parse(s,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            System.out.println("00000");
        }
        return null;
    }
}
