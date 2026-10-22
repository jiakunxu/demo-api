package com.example.demo.banner.service;

import com.example.demo.banner.api.bo.Banner;
import com.example.demo.banner.dao.dataobject.BannerDO;
import com.example.demo.banner.dao.mapper.BannerMapper;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
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

class BannerServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final BannerMapper mapper = mock(BannerMapper.class);
    private final BannerServiceImpl service = spy(new BannerServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, BannerDO.class); }

    @Test
    void missingQueryReturnsDefaultsWithoutDatabaseAccess() {
        assertThat(service.countBanner(null)).isZero();
        assertThat(service.listBanners(null)).isEmpty();
        assertThat(service.getBanner(" ")).isNull();
        assertThat(service.getBanner((BigInteger) null)).isNull();
        verifyNoInteractions(mapper);
    }

    @Test
    void countUsesLongAndPreservesSearchCriteria() {
        Banner query = new Banner(); query.setSearch("needle");
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countBanner(query)).isEqualTo(3000000000L);
        var capture = ArgumentCaptor.forClass(BannerDO.class);
        verify(mapper).count(capture.capture());
        assertThat(capture.getValue().getSearch()).isEqualTo("needle");
    }

    @Test
    void listMapsIdentifiersAndPreservesPagination() {
        Banner query = new Banner(); query.setPageNo(2L); query.setPageSize(20L);
        BannerDO row = new BannerDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listBanners(query)).extracting(Banner::getId).containsExactly(id);
        var capture = ArgumentCaptor.forClass(BannerDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowsProduceEmptyList(boolean nullList) {
        when(mapper.list(any())).thenReturn(nullList ? null : List.of());
        assertThat(service.listBanners(new Banner())).isEmpty();
    }

    @Test
    void lookupByStringOrNumericIdMapsStoredRow() {
        BannerDO row = new BannerDO(); row.setId(id);
        doReturn(row).when(service).getById(id);
        assertThat(service.getBanner("10").getId()).isEqualTo(id);
        assertThat(service.getBanner(id).getId()).isEqualTo(id);
        doReturn(null).when(service).getById(id);
        assertThat(service.getBanner("10")).isNull();
    }

    @Test
    void insertionSetsCreatorAndReturnsGeneratedIdentifier() {
        doAnswer(invocation -> { BannerDO row = invocation.getArgument(0); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(BannerDO.class));
        Banner entity = new Banner();
        assertThat(service.insertBanner(entity, "creator")).isSameAs(entity);
        assertThat(entity.getId()).isEqualTo(id);
    }

    @Test
    void databaseInsertionFailurePropagates() {
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(service).save(any(BannerDO.class));
        assertThatThrownBy(() -> service.insertBanner(new Banner(), "creator")).isSameAs(failure);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateScopesIdentifierAndSetsModifierOrRejectsNoAffectedRows(boolean success) {
        doReturn(success).when(service).updateById(any(BannerDO.class));
        Banner changed = new Banner();
        if (success) assertThat(service.updateBanner(id, changed, "editor")).isSameAs(changed);
        else assertThatThrownBy(() -> service.updateBanner(id, changed, "editor")).isInstanceOf(ServiceException.class);
        var capture = ArgumentCaptor.forClass(BannerDO.class);
        verify(service).updateById(capture.capture());
        assertThat(capture.getValue().getId()).isEqualTo(id);
        assertThat(capture.getValue().getUpdateBy()).isEqualTo("editor");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteUsesLogicalFlagAndRequestedIdentifier(boolean success) {
        doReturn(success).when(service).update(any(Wrapper.class));
        if (success) assertThat(service.deleteBanner(id, "editor").getId()).isEqualTo(id);
        else assertThatThrownBy(() -> service.deleteBanner(id, "editor")).isInstanceOf(ServiceException.class);
        ArgumentCaptor<Wrapper<BannerDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<BannerDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(id, true, "editor");
    }

}
