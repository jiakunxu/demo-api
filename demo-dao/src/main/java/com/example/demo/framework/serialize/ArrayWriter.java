package com.example.demo.framework.serialize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;

public class ArrayWriter implements ObjectWriter<String> {

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType,
                      long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        jsonWriter.write(JSON.parseArray((String) object));
    }

}
