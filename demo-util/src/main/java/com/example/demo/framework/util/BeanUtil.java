package com.example.demo.framework.util;

import com.alibaba.fastjson2.JSON;
import org.jspecify.annotations.NonNull;
import org.springframework.cglib.beans.BeanCopier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author JiakunXu
 */
public class BeanUtil {

    /**
     * 
     * @param source
     * @param target
     * @return
     * @param <T>
     */
    public static <T> T copy(Object source, @NonNull Class<T> target) {
        return source == null ? null : JSON.parseObject(JSON.toJSONString(source), target);
    }

    /**
     * 
     * @param source
     * @param target
     * @return
     * @param <T>
     */
    public static <T> List<T> copy(List<?> source, @NonNull Class<T> target) {
        return source == null ? null : JSON.parseArray(JSON.toJSONString(source), target);
    }

    /**
     * 
     * @param source
     * @param target
     * @return
     * @param <T>
     */
    public static <T> T copy(Object source, @NonNull Supplier<T> target) {
        if (source == null) {
            return null;
        }

        T object = target.get();

        BeanCopier.create(source.getClass(), object.getClass(), false).copy(source, object, null);

        return object;
    }

    /**
     * 
     * @param source
     * @param target
     * @return
     * @param <T>
     */
    public static <T> List<T> copy(List<?> source, @NonNull Supplier<T> target) {
        if (source == null) {
            return null;
        }

        List<T> list = new ArrayList<>();

        if (source.isEmpty()) {
            return list;
        }

        BeanCopier copier = BeanCopier.create(source.getFirst().getClass(), target.get().getClass(),
            false);

        source.forEach(item -> {
            T object = target.get();
            copier.copy(item, object, null);
            list.add(object);
        });

        return list;
    }

}
