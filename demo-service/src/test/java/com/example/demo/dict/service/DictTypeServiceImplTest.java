package com.example.demo.dict.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.cache.api.RedisService;
import com.example.demo.dict.api.*;
import com.example.demo.dict.api.bo.*;
import com.example.demo.dict.dao.dataobject.*;
import com.example.demo.dict.dao.mapper.*;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import com.example.demo.support.TransactionProbe;
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

class DictTypeServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final DictTypeMapper mapper = mock(DictTypeMapper.class);
    private final DictDataService data = mock(DictDataService.class);
    private final RedisService<String, DictType> redis = mock(RedisService.class);
    private final DictTypeServiceImpl service = spy(new DictTypeServiceImpl());
    private final String key = RedisService.CACHE_KEY_DICT_TYPE + "10@color";
    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, DictTypeDO.class);
        ReflectionTestUtils.setField(service, "redisService", redis);
        ReflectionTestUtils.setField(service, "dictDataService", data);
    }
    private DictType before() { DictType type = new DictType(); type.setId(id); type.setValue("color"); return type; }

    @Test
    void cachedTypeAvoidsDatabaseQuery() {
        DictType cached = before(); when(redis.get(key)).thenReturn(cached);
        assertThat(service.getType("10", "color")).isSameAs(cached);
        verify(service, never()).getOne(any(Wrapper.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void cacheMissOrReadFailureUsesBothQueryConstraints(boolean failure) {
        if (failure) when(redis.get(key)).thenThrow(new ServiceException(503, "offline"));
        DictTypeDO row = new DictTypeDO(); row.setId(id); row.setValue("color");
        doReturn(row).when(service).getOne(any(Wrapper.class));
        DictType result = service.getType("10", "color");
        assertThat(result.getId()).isEqualTo(id);
        ArgumentCaptor<Wrapper<DictTypeDO>> capture = ArgumentCaptor.captor();
        verify(service).getOne(capture.capture());
        assertThat(capture.getValue().getSqlSegment()).contains("id", "value");
        var wrapper = (com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DictTypeDO>) capture.getValue();
        assertThat(wrapper.getParamNameValuePairs().values()).contains(id, "color");
        verify(redis).set(key, result);
    }

    @Test
    void missingTypeIsNotCachedAndWriteFailureStillReturnsType() {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getType("10", "color")).isNull();
        verify(redis, never()).set(anyString(), any());
        DictTypeDO row = new DictTypeDO(); row.setId(id);
        doReturn(row).when(service).getOne(any(Wrapper.class));
        doThrow(new ServiceException(503, "offline")).when(redis).set(eq(key), any());
        assertThat(service.getType("10", "color").getId()).isEqualTo(id);
    }

    @Test
    void blankQueriesReturnDefaultsWithoutDependencies() {
        assertThat(service.countType(null)).isZero();
        assertThat(service.listTypes(null)).isEmpty();
        assertThat(service.getType((String) null, " ")).isNull();
        assertThat(service.getType((BigInteger) null, "")).isNull();
        verifyNoInteractions(mapper, redis);
    }

    @Test
    void largeCountAndMapperRowsAreConverted() {
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countType(new DictType())).isEqualTo(3000000000L);
        DictTypeDO row = new DictTypeDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listTypes(new DictType())).extracting(DictType::getId).containsExactly(id);
    }

    @Test
    void insertionPreservesCreatorAndGeneratedId() {
        doAnswer(invocation -> { DictTypeDO row = invocation.getArgument(0); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(DictTypeDO.class));
        DictType type = new DictType();
        assertThat(service.insertType(type, "creator")).isSameAs(type);
        assertThat(type.getId()).isEqualTo(id);
    }

    private void verifyInvalidatedKeys() {
        verify(redis).remove(RedisService.CACHE_KEY_DICT_TYPE + "10@color");
        verify(redis).remove(RedisService.CACHE_KEY_DICT_TYPE + "10@null");
        verify(redis).remove(RedisService.CACHE_KEY_DICT_TYPE + "null@color");
    }

    @Test
    void renameUpdatesChildItemsAndInvalidatesAllLookupVariants() {
        doReturn(before()).when(service).getType(id, null);
        doReturn(true).when(service).updateById(any(DictTypeDO.class));
        DictType changed = new DictType(); changed.setValue("new-color");
        assertThat(service.updateType(id, changed, "editor")).isSameAs(changed);
        verify(data).updateData(id, "new-color", "editor");
        verifyInvalidatedKeys();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingTypeOrFailedUpdateDoesNotTouchChildrenOrCache(boolean missing) {
        doReturn(missing ? null : before()).when(service).getType(id, null);
        if (!missing) doReturn(false).when(service).updateById(any(DictTypeDO.class));
        assertThatThrownBy(() -> service.updateType(id, new DictType(), "editor")).isInstanceOf(ServiceException.class);
        verifyNoInteractions(data, redis);
    }

    @Test
    void childUpdateFailureCausesTransactionRollbackBeforeCacheInvalidation() {
        doReturn(before()).when(service).getType(id, null);
        doReturn(true).when(service).updateById(any(DictTypeDO.class));
        doThrow(new ServiceException(500, "child failure")).when(data).updateData(id, "new-color", "editor");
        DictType changed = new DictType(); changed.setValue("new-color");
        var transactions = new TransactionProbe();
        assertThatThrownBy(() -> transactions.wrap(service).updateType(id, changed, "editor")).hasMessage("child failure");
        assertThat(transactions.rollbacks).isEqualTo(1);
        verifyNoInteractions(redis);
    }

    @Test
    void deletionScopesTypeCascadesToDataAndClearsCache() {
        doReturn(before()).when(service).getType(id, null);
        doReturn(true).when(service).update(any(Wrapper.class));
        assertThat(service.deleteType(id, "editor").getId()).isEqualTo(id);
        verify(data).deleteData(id, null, "editor");
        verifyInvalidatedKeys();
        ArgumentCaptor<Wrapper<DictTypeDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<DictTypeDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(id, true, "editor");
    }

    @Test
    void invalidationFailureDoesNotStopRemainingKeysOrSuccessfulRename() {
        doReturn(before()).when(service).getType(id, null);
        doReturn(true).when(service).updateById(any(DictTypeDO.class));
        doThrow(new RuntimeException("offline")).when(redis).remove(key);
        DictType changed = new DictType(); changed.setValue("new-color");
        assertThatCode(() -> service.updateType(id, changed, "editor")).doesNotThrowAnyException();
        verifyInvalidatedKeys();
    }
}
