package com.example.demo.corp.service;

import com.example.demo.corp.api.bo.Corp;
import com.example.demo.corp.dao.dataobject.CorpDO;
import com.example.demo.corp.dao.mapper.CorpMapper;
import com.example.demo.framework.exception.ServiceException;
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

class CorpServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final CorpMapper mapper = mock(CorpMapper.class);
    private final CorpServiceImpl service = spy(new CorpServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, CorpDO.class); }

    @Test
    void missingQueryReturnsDefaultsWithoutDatabaseAccess() {
        assertThat(service.countCorp(null)).isZero();
        assertThat(service.listCorps(null)).isEmpty();
        assertThat(service.getCorp(" ")).isNull();
        assertThat(service.getCorp((BigInteger) null)).isNull();
        verifyNoInteractions(mapper);
    }

    @Test
    void countUsesLongAndPreservesSearchCriteria() {
        Corp query = new Corp(); query.setSearch("needle");
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countCorp(query)).isEqualTo(3000000000L);
        var capture = ArgumentCaptor.forClass(CorpDO.class);
        verify(mapper).count(capture.capture());
        assertThat(capture.getValue().getSearch()).isEqualTo("needle");
    }

    @Test
    void listMapsIdentifiersAndPreservesPagination() {
        Corp query = new Corp(); query.setPageNo(2L); query.setPageSize(20L);
        CorpDO row = new CorpDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listCorps(query)).extracting(Corp::getId).containsExactly(id);
        var capture = ArgumentCaptor.forClass(CorpDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowsProduceEmptyList(boolean nullList) {
        when(mapper.list(any())).thenReturn(nullList ? null : List.of());
        assertThat(service.listCorps(new Corp())).isEmpty();
    }

    @Test
    void lookupByStringOrNumericIdMapsStoredRow() {
        CorpDO row = new CorpDO(); row.setId(id);
        doReturn(row).when(service).getById(id);
        assertThat(service.getCorp("10").getId()).isEqualTo(id);
        assertThat(service.getCorp(id).getId()).isEqualTo(id);
        doReturn(null).when(service).getById(id);
        assertThat(service.getCorp("10")).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateScopesIdentifierAndSetsModifierOrRejectsNoAffectedRows(boolean success) {
        doReturn(success).when(service).updateById(any(CorpDO.class));
        Corp changed = new Corp();
        if (success) assertThat(service.updateCorp(id, changed, "editor")).isSameAs(changed);
        else assertThatThrownBy(() -> service.updateCorp(id, changed, "editor")).isInstanceOf(ServiceException.class);
        var capture = ArgumentCaptor.forClass(CorpDO.class);
        verify(service).updateById(capture.capture());
        assertThat(capture.getValue().getId()).isEqualTo(id);
        assertThat(capture.getValue().getUpdateBy()).isEqualTo("editor");
    }

    @Test
    void emptyCorpNameDoesNotEraseStoredName() {
        doReturn(true).when(service).updateById(any(CorpDO.class));
        Corp changed = new Corp(); changed.setName("");
        service.updateCorp(id, changed, "editor");
        verify(service).updateById(argThat(row -> row.getName() == null));
    }

}
