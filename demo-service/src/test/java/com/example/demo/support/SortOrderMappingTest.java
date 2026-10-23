package com.example.demo.support;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.example.demo.dict.api.bo.DictData;
import com.example.demo.dict.dao.dataobject.DictDataDO;
import com.example.demo.framework.dataobject.BaseDO;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.menu.api.bo.Menu;
import com.example.demo.menu.dao.dataobject.MenuDO;
import com.example.demo.role.api.bo.Role;
import com.example.demo.role.dao.dataobject.RoleDO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.BeanWrapperImpl;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SortOrderMappingTest {

    static Stream<Arguments> entities() {
        return Stream.of(
            Arguments.of(Menu.class, MenuDO.class, "menu", "Menu", "list"),
            Arguments.of(Role.class, RoleDO.class, "role", "Role", "list"),
            Arguments.of(DictData.class, DictDataDO.class, "dict", "DictData", "list"));
    }

    @ParameterizedTest
    @MethodSource("entities")
    void sortOrderSurvivesConversionAndUsesRenamedColumn(Class<?> businessClass,
                                                        Class<? extends BaseDO> entityClass,
                                                        String domain, String name,
                                                        String listMethod) throws Exception {
        Object business = businessClass.getDeclaredConstructor().newInstance();
        new BeanWrapperImpl(business).setPropertyValue("sortOrder", 17);
        BaseDO entity = BeanUtil.copy(business, entityClass);
        assertThat(new BeanWrapperImpl(entity).getPropertyValue("sortOrder")).isEqualTo(17);
        Object result = BeanUtil.copy(entity, businessClass);
        var json = JSON.parseObject(JSON.toJSONString(result));
        assertThat(json.getInteger("sortOrder")).isEqualTo(17);
        assertThat(json).doesNotContainKey("order");

        MybatisConfiguration configuration = new MybatisConfiguration();
        var table = TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(configuration, "sort-order-test"), entityClass);
        assertThat(table.getFieldList()).anySatisfy(field -> {
            assertThat(field.getProperty()).isEqualTo("sortOrder");
            assertThat(field.getColumn()).isEqualTo("sort_order");
        });
        assertThat(table.getFieldList()).noneSatisfy(field ->
            assertThat(field.getProperty()).isEqualTo("order"));

        configuration.getTypeAliasRegistry().registerAlias(entityClass);
        String resource = "mappers/" + name + "Mapper.xml";
        try (InputStream input = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource,
                configuration.getSqlFragments()).parse();
        }
        entity.setPageNo(1L);
        entity.setPageSize(20L);
        String namespace = "com.example.demo." + domain + ".dao.mapper." + name + "Mapper.";
        String sql = configuration.getMappedStatement(namespace + listMethod)
            .getBoundSql(entity).getSql();
        assertThat(sql).contains("t.`sort_order`").doesNotContain("`order`");
        assertThat(sql.substring(sql.indexOf("ORDER BY"))).contains("t.`sort_order`");
    }
}
