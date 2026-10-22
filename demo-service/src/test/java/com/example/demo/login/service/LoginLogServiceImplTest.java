package com.example.demo.login.service;

import com.example.demo.login.api.bo.LoginLog;
import com.example.demo.login.dao.dataobject.LoginLogDO;
import com.example.demo.login.dao.mapper.LoginLogMapper;
import com.example.demo.support.ServiceTestSupport;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginLogServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final LoginLogMapper mapper = mock(LoginLogMapper.class);
    private final LoginLogServiceImpl service = spy(new LoginLogServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, LoginLogDO.class); }

    @Test
    void missingQueryReturnsDefaultsWithoutDatabaseAccess() {
        assertThat(service.countLog(null)).isZero();
        assertThat(service.listLogs(null)).isEmpty();
        assertThat(service.getLog(" ")).isNull();
        assertThat(service.getLog((BigInteger) null)).isNull();
        verifyNoInteractions(mapper);
    }

    @Test
    void countUsesLongAndPreservesSearchCriteria() {
        LoginLog query = new LoginLog(); query.setSearch("needle");
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countLog(query)).isEqualTo(3000000000L);
        var capture = ArgumentCaptor.forClass(LoginLogDO.class);
        verify(mapper).count(capture.capture());
        assertThat(capture.getValue().getSearch()).isEqualTo("needle");
    }

    @Test
    void listMapsIdentifiersAndPreservesPagination() {
        LoginLog query = new LoginLog(); query.setPageNo(2L); query.setPageSize(20L);
        LoginLogDO row = new LoginLogDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listLogs(query)).extracting(LoginLog::getId).containsExactly(id);
        var capture = ArgumentCaptor.forClass(LoginLogDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowsProduceEmptyList(boolean nullList) {
        when(mapper.list(any())).thenReturn(nullList ? null : List.of());
        assertThat(service.listLogs(new LoginLog())).isEmpty();
    }

    @Test
    void lookupByStringOrNumericIdMapsStoredRow() {
        LoginLogDO row = new LoginLogDO(); row.setId(id);
        doReturn(row).when(service).getById(id);
        assertThat(service.getLog("10").getId()).isEqualTo(id);
        assertThat(service.getLog(id).getId()).isEqualTo(id);
        doReturn(null).when(service).getById(id);
        assertThat(service.getLog("10")).isNull();
    }

    @Test
    void insertionSetsCreateByAndCopiesGeneratedIdentifier() {
        doAnswer(invocation -> { LoginLogDO row = invocation.getArgument(0); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(LoginLogDO.class));
        LoginLog entity = new LoginLog();
        service.insertLog(entity, "creator");
        assertThat(entity.getId()).isEqualTo(id);
    }

    @Test
    void databaseInsertionFailurePropagates() {
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(service).save(any(LoginLogDO.class));
        assertThatThrownBy(() -> service.insertLog(new LoginLog(), "creator")).isSameAs(failure);
    }

}
