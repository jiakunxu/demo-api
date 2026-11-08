package com.example.demo.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.example.demo.framework.mybatis.type.WildcardTypeHandler;
import com.example.demo.user.dao.dataobject.UserDO;
import com.example.demo.user.dao.dataobject.UserRoleDO;
import com.example.demo.user.dao.mapper.UserRoleMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserRoleQueryMappingTest {

    private static MybatisConfiguration configuration;

    @BeforeAll
    static void loadMapper() throws Exception {
        configuration = new MybatisConfiguration();
        configuration.getTypeAliasRegistry().registerAlias(UserDO.class);
        configuration.getTypeAliasRegistry().registerAlias(UserRoleDO.class);
        String resource = "mappers/UserRoleMapper.xml";
        try (InputStream input = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource,
                configuration.getSqlFragments()).parse();
        }
    }

    static Stream<Arguments> filters() {
        return Stream.of(true, false).flatMap(exists ->
            Stream.of(null, "", "138%_").map(phone -> Arguments.of(exists, phone)));
    }

    @ParameterizedTest
    @MethodSource("filters")
    void countAndListBindTheSamePhoneAndRoleFilters(boolean exists, String phone)
        throws Exception {
        UserDO query = new UserDO();
        query.setCorpId(BigInteger.TEN);
        query.setRoleId(BigInteger.ONE);
        query.setExists(exists);
        query.setEnabled(true);
        query.setName("张");
        query.setPhone(phone);
        query.setPageNo(2L);
        query.setPageSize(10L);

        MappedStatement count = statement("countUser");
        MappedStatement list = statement("listUsers");
        String countSql = normalizedSql(count.getBoundSql(query));
        String listSql = normalizedSql(list.getBoundSql(query));

        assertThat(count.getResultMaps().getFirst().getType()).isEqualTo(Long.class);
        assertThat(list.getResultMaps().getFirst().getType()).isEqualTo(UserDO.class);
        assertThat(listSql.substring(0, listSql.indexOf(" FROM "))).contains("t.`phone`");
        assertThat(countSql).doesNotContain("ORDER BY", "LIMIT", "mobile");
        assertThat(listSql).endsWith("ORDER BY t.`id` DESC LIMIT ?, ?")
            .doesNotContain("mobile");
        assertThat(listSql.substring(listSql.indexOf(" WHERE "), listSql.indexOf(" ORDER BY ")))
            .isEqualTo(countSql.substring(countSql.indexOf(" WHERE ")));
        assertThat(countSql).contains("t.`corp_id` = ?", "t.`is_deleted` = 0",
            "p.`is_deleted` = 0", "q.`is_deleted` = 0", "p.`role_id` = ?",
            "t.`is_enabled` = ?", "t.`name` LIKE ?",
            exists ? "AND EXISTS(" : "AND NOT EXISTS(");

        for (MappedStatement statement : List.of(count, list)) {
            BoundSql boundSql = statement.getBoundSql(query);
            var mappings = boundSql.getParameterMappings();
            if (phone == null || phone.isEmpty()) {
                assertThat(boundSql.getSql()).doesNotContain("t.`phone` LIKE");
                assertThat(mappings).noneSatisfy(mapping ->
                    assertThat(mapping.getProperty()).isEqualTo("phone"));
            } else {
                assertThat(boundSql.getSql()).contains("t.`phone` LIKE ?");
                var phoneMapping = mappings.stream()
                    .filter(mapping -> mapping.getProperty().equals("phone"))
                    .findFirst().orElseThrow();
                assertThat(phoneMapping.getTypeHandler()).isInstanceOf(WildcardTypeHandler.class);
                PreparedStatement prepared = mock(PreparedStatement.class);
                configuration.newParameterHandler(statement, query, boundSql)
                    .setParameters(prepared);
                verify(prepared).setString(mappings.indexOf(phoneMapping) + 1, "%138\\%\\_%");
            }
        }
    }

    private static MappedStatement statement(String method) {
        return configuration.getMappedStatement(UserRoleMapper.class.getName() + "." + method);
    }

    private static String normalizedSql(BoundSql boundSql) {
        return boundSql.getSql().replaceAll("\\s+", " ").trim();
    }
}
