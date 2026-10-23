package com.example.demo.config.service;

import com.example.demo.config.api.bo.Config;
import com.example.demo.config.dao.dataobject.ConfigDO;
import com.example.demo.config.dao.mapper.ConfigMapper;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final ConfigMapper mapper = mock(ConfigMapper.class);
    private final ConfigServiceImpl service = spy(new ConfigServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, ConfigDO.class); }

    @Test
    void missingQueryReturnsDefaultsWithoutDatabaseAccess() {
        assertThat(service.countConfig(null)).isZero();
        assertThat(service.listConfigs(null)).isEmpty();
        assertThat(service.getConfig(null, " ")).isNull();
        verifyNoInteractions(mapper);
    }

    @Test
    void countUsesLongAndPreservesSearchCriteria() {
        Config query = new Config(); query.setSearch("needle");
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countConfig(query)).isEqualTo(3000000000L);
        var capture = ArgumentCaptor.forClass(ConfigDO.class);
        verify(mapper).count(capture.capture());
        assertThat(capture.getValue().getSearch()).isEqualTo("needle");
    }

    @Test
    void listMapsIdentifiersAndPreservesPagination() {
        Config query = new Config(); query.setPageNo(2L); query.setPageSize(20L);
        ConfigDO row = new ConfigDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listConfigs(query)).extracting(Config::getId).containsExactly(id);
        var capture = ArgumentCaptor.forClass(ConfigDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowsProduceEmptyList(boolean nullList) {
        when(mapper.list(any())).thenReturn(nullList ? null : List.of());
        assertThat(service.listConfigs(new Config())).isEmpty();
    }

    @Test
    void lookupIncludesIdAndKeyOrUsesKeyAlone() {
        ConfigDO row = new ConfigDO();
        row.setId(id);
        doReturn(row).when(service).getOne(any(Wrapper.class));

        assertThat(service.getConfig("10", "site-name").getId()).isEqualTo(id);
        assertThat(service.getConfig(null, "site-name").getId()).isEqualTo(id);

        ArgumentCaptor<Wrapper<ConfigDO>> capture = ArgumentCaptor.captor();
        verify(service, times(2)).getOne(capture.capture());
        var both = (LambdaQueryWrapper<ConfigDO>) capture.getAllValues().getFirst();
        assertThat(both.getSqlSegment()).contains("id =", "AND", "`key` =");
        assertThat(both.getParamNameValuePairs().values()).containsExactlyInAnyOrder(id, "site-name");
        var keyOnly = (LambdaQueryWrapper<ConfigDO>) capture.getAllValues().get(1);
        assertThat(keyOnly.getSqlSegment()).doesNotContain("id =").contains("`key` =");
        assertThat(keyOnly.getParamNameValuePairs().values()).containsExactly("site-name");
        assertThat(both.getSqlSelect()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void idLookupIgnoresEmptyAndWhitespaceKey(String key) {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getConfig("10", key)).isNull();

        ArgumentCaptor<Wrapper<ConfigDO>> capture = ArgumentCaptor.captor();
        verify(service).getOne(capture.capture());
        var wrapper = (LambdaQueryWrapper<ConfigDO>) capture.getValue();
        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("id =").doesNotContain("`key` =");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactly(id);
    }

    @Test
    void invalidIdAndMultipleResultsAreNotSilentlyIgnored() {
        assertThatThrownBy(() -> service.getConfig("invalid", "site-name"))
            .isInstanceOf(NumberFormatException.class);
        verifyNoInteractions(mapper);
        doThrow(new org.apache.ibatis.exceptions.TooManyResultsException("multiple configs"))
            .when(service).getOne(any(Wrapper.class));
        assertThatThrownBy(() -> service.getConfig(null, "site-name"))
            .isInstanceOf(org.apache.ibatis.exceptions.TooManyResultsException.class);
    }

    @Test
    void insertionSetsCreatorAndReturnsGeneratedIdentifier() {
        doAnswer(invocation -> { ConfigDO row = invocation.getArgument(0); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(ConfigDO.class));
        Config entity = new Config();
        assertThat(service.insertConfig(entity, "creator")).isSameAs(entity);
        assertThat(entity.getId()).isEqualTo(id);
    }

    @Test
    void databaseInsertionFailurePropagates() {
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(service).save(any(ConfigDO.class));
        assertThatThrownBy(() -> service.insertConfig(new Config(), "creator")).isSameAs(failure);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateScopesIdentifierAndSetsModifierOrRejectsNoAffectedRows(boolean success) {
        doReturn(success).when(service).updateById(any(ConfigDO.class));
        Config changed = new Config();
        if (success) assertThat(service.updateConfig(id, changed, "editor")).isSameAs(changed);
        else assertThatThrownBy(() -> service.updateConfig(id, changed, "editor")).isInstanceOf(ServiceException.class);
        var capture = ArgumentCaptor.forClass(ConfigDO.class);
        verify(service).updateById(capture.capture());
        assertThat(capture.getValue().getId()).isEqualTo(id);
        assertThat(capture.getValue().getUpdateBy()).isEqualTo("editor");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteUsesLogicalFlagAndRequestedIdentifier(boolean success) {
        doReturn(success).when(service).update(any(Wrapper.class));
        if (success) assertThat(service.deleteConfig(id, "editor").getId()).isEqualTo(id);
        else assertThatThrownBy(() -> service.deleteConfig(id, "editor")).isInstanceOf(ServiceException.class);
        ArgumentCaptor<Wrapper<ConfigDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<ConfigDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(id, true, "editor");
    }

}
