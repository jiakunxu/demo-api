package com.example.demo.framework.serialize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

public class ObjectWriter implements com.alibaba.fastjson2.writer.ObjectWriter<String> {

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType,
                      long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.write(JSON.parseObject((String) object));
    }

}
