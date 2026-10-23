package com.example.demo.log.service;

import com.example.demo.operate.api.bo.OperateLog;
import com.example.demo.operate.dao.dataobject.OperateLogDO;
import com.example.demo.operate.dao.mapper.OperateLogMapper;
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

class OperateLogServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final OperateLogMapper mapper = mock(OperateLogMapper.class);
    private final OperateLogServiceImpl service = spy(new OperateLogServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, OperateLogDO.class); }

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
        OperateLog query = new OperateLog(); query.setSearch("needle");
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countLog(query)).isEqualTo(3000000000L);
        var capture = ArgumentCaptor.forClass(OperateLogDO.class);
        verify(mapper).count(capture.capture());
        assertThat(capture.getValue().getSearch()).isEqualTo("needle");
    }

    @Test
    void listMapsIdentifiersAndPreservesPagination() {
        OperateLog query = new OperateLog(); query.setPageNo(2L); query.setPageSize(20L);
        OperateLogDO row = new OperateLogDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listLogs(query)).extracting(OperateLog::getId).containsExactly(id);
        var capture = ArgumentCaptor.forClass(OperateLogDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowsProduceEmptyList(boolean nullList) {
        when(mapper.list(any())).thenReturn(nullList ? null : List.of());
        assertThat(service.listLogs(new OperateLog())).isEmpty();
    }

    @Test
    void lookupByStringOrNumericIdMapsStoredRow() {
        OperateLogDO row = new OperateLogDO(); row.setId(id);
        doReturn(row).when(service).getById(id);
        assertThat(service.getLog("10").getId()).isEqualTo(id);
        assertThat(service.getLog(id).getId()).isEqualTo(id);
        doReturn(null).when(service).getById(id);
        assertThat(service.getLog("10")).isNull();
    }

    @Test
    void insertionSetsCreateByAndCopiesGeneratedIdentifier() {
        doAnswer(invocation -> { OperateLogDO row = invocation.getArgument(0); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(OperateLogDO.class));
        OperateLog entity = new OperateLog();
        service.insertLog(entity, "creator");
        assertThat(entity.getId()).isEqualTo(id);
    }

    @Test
    void databaseInsertionFailurePropagates() {
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(service).save(any(OperateLogDO.class));
        assertThatThrownBy(() -> service.insertLog(new OperateLog(), "creator")).isSameAs(failure);
    }

    @Test
    void defaultSortAndAnonymousDisplayAreNormalized() {
        OperateLogDO row = new OperateLogDO(); row.setOperator("anonymous");
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listLogs(new OperateLog()).getFirst().getOperator()).isEmpty();
        var capture = ArgumentCaptor.forClass(OperateLogDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getSort()).isEqualTo("id");
        assertThat(capture.getValue().getDir()).isEqualTo("DESC");
        doReturn(row).when(service).getById(id);
        assertThat(service.getLog("10").getOperator()).isEmpty();
    }

}
