package com.example.demo.dict.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.cache.api.RedisService;
import com.example.demo.dict.api.*;
import com.example.demo.dict.api.bo.*;
import com.example.demo.dict.dao.dataobject.*;
import com.example.demo.dict.dao.mapper.*;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DictDataServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final BigInteger typeId = BigInteger.ONE;
    private final DictDataMapper mapper = mock(DictDataMapper.class);
    private final RedisService<String, DictData> redis = mock(RedisService.class);
    private final DictDataServiceImpl service = spy(new DictDataServiceImpl());
    private final String key = RedisService.CACHE_KEY_DICT_TYPE + "color&red";

    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, DictDataDO.class);
        ReflectionTestUtils.setField(service, "redisService", redis);
    }
    private DictData before() { DictData data = new DictData();
    data.setId(id);
    data.setTypeValue("color");
    data.setValue("red");
    return data;
    }

    @Test
    void cacheHitAvoidsDatabase() {
        DictData cached = before(); when(redis.get(key)).thenReturn(cached);
        assertThat(service.getData("color", "red")).isSameAs(cached);
        verifyNoInteractions(mapper);
        verify(service, never()).getOne(any(Wrapper.class));
        verify(redis, never()).set(anyString(), any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void cacheMissAndCacheReadFailureBothFallBackToEnabledDatabaseRow(boolean failure) {
        if (failure) when(redis.get(key)).thenThrow(new ServiceException(503, "offline"));
        DictDataDO row = new DictDataDO(); row.setId(id); row.setName("红色");
        doReturn(row).when(service).getOne(any(Wrapper.class));
        DictData result = service.getData("color", "red");
        assertThat(result.getName()).isEqualTo("红色");
        ArgumentCaptor<Wrapper<DictDataDO>> query = ArgumentCaptor.captor();
        verify(service).getOne(query.capture());
        var wrapper = (LambdaQueryWrapper<DictDataDO>) query.getValue();
        assertThat(wrapper.getSqlSegment()).contains("type_value =", "`value` =", "status =")
            .contains("ORDER BY id DESC").endsWith("LIMIT 1");
        assertThat(wrapper.getParamNameValuePairs().values())
            .containsExactlyInAnyOrder("color", "red", DictData.Status.ENABLE.value);
        assertThat(wrapper.getSqlSelect()).isNull();
        verify(redis).set(key, result);
    }

    @Test
    void cacheWriteFailureDoesNotDiscardDatabaseResult() {
        DictDataDO row = new DictDataDO(); row.setId(id);
        doReturn(row).when(service).getOne(any(Wrapper.class));
        doThrow(new ServiceException(503, "offline")).when(redis).set(eq(key), any());
        assertThat(service.getData("color", "red").getId()).isEqualTo(id);
    }

    @Test
    void missingDatabaseRowReturnsEmptyBeanWithoutCaching() {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getData("color", "red").getId()).isNull();
        verify(redis, never()).set(anyString(), any());
    }

    @Test
    void blankCacheLookupAvoidsBothDependencies() {
        assertThat(service.getData("", "red").getId()).isNull();
        assertThat(service.getData("color", " ").getId()).isNull();
        verifyNoInteractions(mapper, redis);
    }

    @Test
    void listOverloadsUseEnabledFilterAndHaveNoArtificialPaginationLimit() {
        service.listDatas("1", "color");
        service.listDatas(null, new String[] {"color", "size"});
        var queries = ArgumentCaptor.forClass(DictDataDO.class);
        verify(mapper, times(2)).listDatas(queries.capture());
        for (DictDataDO query : queries.getAllValues()) {
            assertThat(query.getStatus()).isEqualTo(DictData.Status.ENABLE.value);
            assertThat(query.getPageNo()).isNull(); assertThat(query.getPageSize()).isNull();
        }
        assertThat(queries.getAllValues().getFirst().getTypeId()).isEqualTo(typeId);
        assertThat(queries.getAllValues().getFirst().getTypeValue()).isEqualTo("color");
        assertThat(queries.getAllValues().get(1).getTypeValues()).containsExactly("color", "size");
    }

    @Test
    void invalidCountListAndIdQueriesAvoidDatabase() {
        assertThat(service.countData(null, null, new DictData())).isZero();
        assertThat(service.countData("1", "color", null)).isZero();
        assertThat(service.listDatas(null, (String) null)).isEmpty();
        assertThat(service.listDatas(null, new String[0])).isEmpty();
        assertThat(service.getData((BigInteger) null)).isNull();
        assertThat(service.getData(" ")).isNull();
        verifyNoInteractions(mapper);
    }

    @Test
    void countRemainsLongAndListMapsRows() {
        when(mapper.countData(any())).thenReturn(3000000000L);
        assertThat(service.countData("1", "color", new DictData())).isEqualTo(3000000000L);
        DictDataDO row = new DictDataDO(); row.setId(id);
        when(mapper.listDatas(any())).thenReturn(List.of(row));
        assertThat(service.listDatas("1", "color")).extracting(DictData::getId).containsExactly(id);
    }

    @Test
    void insertSetsTypeAndCreatorAndReturnsGeneratedId() {
        doAnswer(invocation -> { DictDataDO row = invocation.getArgument(0); assertThat(row.getTypeId()).isEqualTo(typeId); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(DictDataDO.class));
        DictData data = new DictData();
        assertThat(service.insertData(typeId, data, "creator")).isSameAs(data);
        assertThat(data.getId()).isEqualTo(id);
    }

    @Test
    void updateKeepsTypeOwnershipAndInvalidatesOldCacheKey() {
        doReturn(before()).when(service).getData(id);
        doReturn(true).when(service).updateById(any(DictDataDO.class));
        DictData changed = new DictData();
        changed.setTypeId(BigInteger.valueOf(99));
        changed.setTypeValue("forged");
        changed.setValue("blue");
        assertThat(service.updateData(id, changed, "editor")).isSameAs(changed);
        var rows = ArgumentCaptor.forClass(DictDataDO.class);
        verify(service).updateById(rows.capture());
        assertThat(rows.getValue().getTypeId()).isNull(); assertThat(rows.getValue().getTypeValue()).isNull();
        assertThat(rows.getValue().getId()).isEqualTo(id);
        assertThat(rows.getValue().getUpdateBy()).isEqualTo("editor");
        verify(redis).remove(key);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowOrFailedUpdateDoesNotInvalidateCache(boolean missing) {
        doReturn(missing ? null : before()).when(service).getData(id);
        if (!missing) doReturn(false).when(service).updateById(any(DictDataDO.class));
        assertThatThrownBy(() -> service.updateData(id, new DictData(), "editor")).isInstanceOf(ServiceException.class);
        verifyNoInteractions(redis);
    }

    @Test
    void cacheInvalidationFailureDoesNotDiscardSuccessfulUpdate() {
        doReturn(before()).when(service).getData(id);
        doReturn(true).when(service).updateById(any(DictDataDO.class));
        doThrow(new RuntimeException("offline")).when(redis).remove(key);
        assertThat(service.updateData(id, new DictData(), "editor").getId()).isEqualTo(id);
    }

    @Test
    void renameTypeInvalidatesEveryOldItemAndUpdatesOnlyThatType() {
        doReturn(List.of(before())).when(service).listDatas("1", (String) null);
        doReturn(true).when(service).update(any(Wrapper.class));
        assertThat(service.updateData(typeId, "new-color", "editor").getTypeValue()).isEqualTo("new-color");
        verify(redis).remove(key);
        ArgumentCaptor<Wrapper<DictDataDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<DictDataDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("type_id");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(typeId, "new-color", "editor");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteOneItemOrWholeTypeUsesCorrespondingConstraint(boolean wholeType) {
        if (wholeType) doReturn(List.of(before())).when(service).listDatas("1", (String) null);
        else doReturn(before()).when(service).getData(id);
        doReturn(true).when(service).update(any(Wrapper.class));
        service.deleteData(wholeType ? typeId : null, wholeType ? null : id, "editor");
        verify(redis).remove(key);
        ArgumentCaptor<Wrapper<DictDataDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<DictDataDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains(wholeType ? "type_id" : "id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(wholeType ? typeId : id, true, "editor");
    }

    @Test
    void deletionWithoutScopeIsRejected() {
        assertThatThrownBy(() -> service.deleteData(null, null, "editor")).hasMessage("参数信息不能为空");
        verifyNoInteractions(mapper, redis);
    }
}
