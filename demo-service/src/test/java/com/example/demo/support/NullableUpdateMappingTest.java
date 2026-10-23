package com.example.demo.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.config.dao.dataobject.ConfigDO;
import com.example.demo.dict.dao.dataobject.DictDataDO;
import com.example.demo.dict.dao.dataobject.DictTypeDO;
import com.example.demo.framework.dataobject.BaseDO;
import com.example.demo.menu.dao.dataobject.MenuDO;
import com.example.demo.role.dao.dataobject.RoleDO;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.BeanWrapperImpl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NullableUpdateMappingTest {

    interface ConfigTestMapper extends BaseMapper<ConfigDO> { }
    interface MenuTestMapper extends BaseMapper<MenuDO> { }
    interface RoleTestMapper extends BaseMapper<RoleDO> { }
    interface DictDataTestMapper extends BaseMapper<DictDataDO> { }
    interface DictTypeTestMapper extends BaseMapper<DictTypeDO> { }

    record UpdateCase(Class<?> mapper, Class<? extends BaseDO> entity, List<String> fields) { }

    static Stream<UpdateCase> entities() {
        return Stream.of(
            new UpdateCase(ConfigTestMapper.class, ConfigDO.class, List.of("remark")),
            new UpdateCase(MenuTestMapper.class, MenuDO.class,
                List.of("icon", "path", "component", "query", "code")),
            new UpdateCase(RoleTestMapper.class, RoleDO.class, List.of("remark")),
            new UpdateCase(DictDataTestMapper.class, DictDataDO.class, List.of("remark")),
            new UpdateCase(DictTypeTestMapper.class, DictTypeDO.class, List.of("remark")));
    }

    @ParameterizedTest
    @MethodSource("entities")
    void nullValuesRemainBoundInUpdateSet(UpdateCase testCase) throws Exception {
        BaseDO entity = testCase.entity().getDeclaredConstructor().newInstance();
        new BeanWrapperImpl(entity).setPropertyValue("id", BigInteger.TEN);
        BoundSql boundSql = updateSql(testCase, entity);
        String sql = boundSql.getSql();
        String set = sql.substring(sql.indexOf("SET"), sql.indexOf("WHERE"));

        for (String field : testCase.fields()) {
            String column = field.equals("query") ? "`query`" : field;
            assertThat(set).contains(column + "=?");
            assertThat(boundSql.getParameterMappings())
                .anySatisfy(mapping -> assertThat(mapping.getProperty()).isEqualTo("et." + field));
            assertThat(SystemMetaObject.forObject(entity).getValue(field)).isNull();
        }
        assertThat(set).doesNotContain("name=", "create_by=", "create_time=", "update_time=");
        assertThat(sql.substring(sql.indexOf("WHERE"))).contains("id=?");
    }

    @ParameterizedTest
    @MethodSource("entities")
    void nonNullValuesRemainBoundInUpdateSet(UpdateCase testCase) throws Exception {
        BaseDO entity = testCase.entity().getDeclaredConstructor().newInstance();
        var properties = new BeanWrapperImpl(entity);
        properties.setPropertyValue("id", BigInteger.TEN);
        for (String field : testCase.fields()) {
            properties.setPropertyValue(field, "updated-" + field);
        }
        BoundSql boundSql = updateSql(testCase, entity);
        for (String field : testCase.fields()) {
            assertThat(boundSql.getParameterMappings())
                .anySatisfy(mapping -> assertThat(mapping.getProperty()).isEqualTo("et." + field));
            assertThat(SystemMetaObject.forObject(entity).getValue(field))
                .isEqualTo("updated-" + field);
        }
    }

    private BoundSql updateSql(UpdateCase testCase, BaseDO entity) {
        var configuration = new MybatisConfiguration();
        configuration.addMapper(testCase.mapper());
        return configuration.getMappedStatement(testCase.mapper().getName() + ".updateById")
            .getBoundSql(Map.of("et", entity));
    }
}
