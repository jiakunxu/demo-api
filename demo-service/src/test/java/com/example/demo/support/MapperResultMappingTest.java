package com.example.demo.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.example.demo.config.dao.dataobject.ConfigDO;
import com.example.demo.dict.dao.dataobject.DictDataDO;
import com.example.demo.framework.dataobject.BaseDO;
import com.example.demo.menu.dao.dataobject.MenuDO;
import com.example.demo.role.dao.dataobject.RoleDO;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.BeanWrapperImpl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MapperResultMappingTest {

    record MappingCase(Class<? extends BaseDO> entity, String domain, String mapper,
                       String column, String property, int jdbcType) { }

    static Stream<Arguments> mappings() {
        return Stream.of(
            new MappingCase(ConfigDO.class, "config", "Config", "is_system", "system",
                Types.BOOLEAN),
            new MappingCase(MenuDO.class, "menu", "Menu", "is_external", "external",
                Types.BOOLEAN),
            new MappingCase(MenuDO.class, "menu", "Menu", "is_cached", "cached", Types.BOOLEAN),
            new MappingCase(MenuDO.class, "menu", "Menu", "is_hidden", "hidden", Types.BOOLEAN),
            new MappingCase(MenuDO.class, "menu", "Menu", "sort_order", "sortOrder",
                Types.INTEGER),
            new MappingCase(RoleDO.class, "role", "Role", "sort_order", "sortOrder",
                Types.INTEGER),
            new MappingCase(DictDataDO.class, "dict", "DictData", "sort_order", "sortOrder",
                Types.INTEGER))
            .flatMap(testCase -> (testCase.jdbcType() == Types.BOOLEAN
                ? Stream.of(true, false, null) : Stream.of(17, 0, null))
                .map(value -> Arguments.of(testCase, value)));
    }

    @ParameterizedTest
    @MethodSource("mappings")
    void xmlResultsMapDatabaseValuesToEntityProperties(MappingCase testCase, Object value)
        throws Exception {
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.getTypeAliasRegistry().registerAlias(testCase.entity());
        String resource = "mappers/" + testCase.mapper() + "Mapper.xml";
        try (InputStream input = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource,
                configuration.getSqlFragments()).parse();
        }
        String namespace = "com.example.demo." + testCase.domain() + ".dao.mapper."
            + testCase.mapper() + "Mapper.list";
        var statement = configuration.getMappedStatement(namespace);
        BaseDO query = testCase.entity().getDeclaredConstructor().newInstance();
        query.setPageNo(2L);
        query.setPageSize(10L);
        var boundSql = statement.getBoundSql(query);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
        String projection = sql.substring(0, sql.indexOf(" FROM "));
        var column = Pattern.compile("t\\.`" + testCase.column()
            + "`(?: AS `?(\\w+)`?)?").matcher(projection);
        assertThat(column.find()).as("Selected column %s", testCase.column()).isTrue();
        String label = column.group(1) == null ? testCase.column() : column.group(1);

        Statement jdbc = returnedRow(label, testCase.jdbcType(), value);
        var handler = new DefaultResultSetHandler(mock(Executor.class), statement, null,
            null, boundSql, RowBounds.DEFAULT);
        List<Object> results = handler.handleResultSets(jdbc);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isInstanceOf(testCase.entity());
        var entity = new BeanWrapperImpl(results.getFirst());
        assertThat(entity.getPropertyValue("id")).isEqualTo(BigInteger.TEN);
        assertThat(entity.getPropertyValue(testCase.property())).isEqualTo(value);
    }

    private static Statement returnedRow(String label, int jdbcType, Object value)
        throws Exception {
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(metadata.getColumnCount()).thenReturn(2);
        when(metadata.getColumnLabel(1)).thenReturn("id");
        when(metadata.getColumnType(1)).thenReturn(Types.BIGINT);
        when(metadata.getColumnClassName(1)).thenReturn(BigInteger.class.getName());
        when(metadata.getColumnLabel(2)).thenReturn(label);
        when(metadata.getColumnType(2)).thenReturn(jdbcType);
        when(metadata.getColumnClassName(2)).thenReturn(
            (jdbcType == Types.BOOLEAN ? Boolean.class : Integer.class).getName());

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getBigDecimal("id")).thenReturn(BigDecimal.TEN);
        when(resultSet.wasNull()).thenReturn(value == null);
        if (jdbcType == Types.BOOLEAN) {
            when(resultSet.getBoolean(label)).thenReturn(Boolean.TRUE.equals(value));
        } else {
            when(resultSet.getInt(label)).thenReturn(value == null ? 0 : (Integer) value);
        }

        Statement statement = mock(Statement.class);
        when(statement.getResultSet()).thenReturn(resultSet);
        when(statement.getUpdateCount()).thenReturn(-1);
        return statement;
    }
}
