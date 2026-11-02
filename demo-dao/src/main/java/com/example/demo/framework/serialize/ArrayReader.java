package com.example.demo.framework.serialize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;

public class ArrayReader implements ObjectReader<String> {

    @Override
    public String readObject(JSONReader jsonReader, Type fieldType, Object fieldName,
                             long features) {
        return jsonReader.readArray() == null ? null : JSON.toJSONString(jsonReader.readArray());
    }

}
