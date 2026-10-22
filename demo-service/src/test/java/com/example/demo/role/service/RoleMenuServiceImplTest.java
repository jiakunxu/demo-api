package com.example.demo.role.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.role.api.bo.RoleMenu;
import com.example.demo.role.dao.dataobject.RoleMenuDO;
import com.example.demo.role.dao.mapper.RoleMenuMapper;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import com.example.demo.support.TransactionProbe;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleMenuServiceImplTest {
    private final BigInteger roleId = BigInteger.TEN;
    private final RoleMenuMapper mapper = mock(RoleMenuMapper.class);
    private final RoleMenuServiceImpl service = spy(new RoleMenuServiceImpl());
    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, RoleMenuDO.class);
        doReturn(true).when(service).saveBatch(anyCollection());
        doReturn(true).when(service).update(any(Wrapper.class));
    }
    private RoleMenuDO row(int id, int menu) {
        var row = new RoleMenuDO();
        row.setId(BigInteger.valueOf(id));
        row.setRoleId(roleId);
        row.setMenuId(BigInteger.valueOf(menu));
        return row;
    }

    @Test
    void differenceUpdateRetainsExistingAddsOnlyNewAndDeletesOnlyRemovedRows() {
        when(mapper.list(any())).thenReturn(List.of(row(101, 1), row(102, 2)));
        var result = service.updateRoleMenus(roleId, new BigInteger[] {BigInteger.TWO, BigInteger.valueOf(3)}, "editor");
        assertThat(result).extracting(RoleMenu::getMenuId).containsExactly(BigInteger.TWO, BigInteger.valueOf(3));
        ArgumentCaptor<Collection<RoleMenuDO>> batch = ArgumentCaptor.captor();
        verify(service).saveBatch(batch.capture());
        assertThat(batch.getValue()).hasSize(1);
        RoleMenuDO added = batch.getValue().iterator().next();
        assertThat(added.getMenuId()).isEqualTo(BigInteger.valueOf(3));
        assertThat(added.getRoleId()).isEqualTo(roleId);
        assertThat(added.getCreateBy()).isEqualTo("editor");
        ArgumentCaptor<Wrapper<RoleMenuDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var deletion = (LambdaUpdateWrapper<RoleMenuDO>) capture.getValue();
        assertThat(deletion.getSqlSegment()).contains("id IN", "role_id");
        assertThat(deletion.getSqlSet()).contains("deleted", "update_by");
        assertThat(deletion.getParamNameValuePairs().values()).contains(BigInteger.valueOf(101), roleId, true, "editor").doesNotContain(BigInteger.valueOf(102));
    }

    @Test
    void unchangedSelectionDoesNotWriteDatabase() {
        when(mapper.list(any())).thenReturn(List.of(row(101, 1)));
        service.updateRoleMenus(roleId, new BigInteger[] {BigInteger.ONE}, "editor");
        verify(service, never()).saveBatch(anyCollection());
        verify(service, never()).update(any(Wrapper.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void clearingSelectionDeletesAllPreviousRelations(boolean nullSelection) {
        when(mapper.list(any())).thenReturn(List.of(row(101, 1), row(102, 2)));
        assertThat(service.updateRoleMenus(roleId, nullSelection ? null : new BigInteger[0], "editor")).isEmpty();
        verify(service, never()).saveBatch(anyCollection());
        verify(service).update(any(Wrapper.class));
    }

    @Test
    void initialBatchCopiesGeneratedIdsBack() {
        doAnswer(invocation -> {
            Collection<RoleMenuDO> rows = invocation.getArgument(0);
            rows.forEach(row -> row.setId(BigInteger.valueOf(99)));
            return true;
        }).when(service).saveBatch(anyCollection());
        var result = service.updateRoleMenus(roleId, new BigInteger[] {BigInteger.ONE}, "creator");
        assertThat(result.getFirst().getId()).isEqualTo(BigInteger.valueOf(99));
        assertThat(result.getFirst().getRoleId()).isEqualTo(roleId);
        verify(service, never()).update(any(Wrapper.class));
    }

    @Test
    void noExistingAndNoRequestedRelationsDoesNotWrite() {
        assertThat(service.updateRoleMenus(roleId, null, "editor")).isEmpty();
        verify(service, never()).saveBatch(anyCollection());
        verify(service, never()).update(any(Wrapper.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void springProxyCommitsSuccessfulUpdateAndRollsBackDeletionFailure(boolean success) {
        when(mapper.list(any())).thenReturn(List.of(row(101, 1)));
        doReturn(success).when(service).update(any(Wrapper.class));
        var transactions = new TransactionProbe();
        RoleMenuServiceImpl proxy = transactions.wrap(service);
        if (success) proxy.updateRoleMenus(roleId, null, "editor");
        else assertThatThrownBy(() -> proxy.updateRoleMenus(roleId, null, "editor")).isInstanceOf(ServiceException.class);
        assertThat(transactions.commits).isEqualTo(success ? 1 : 0);
        assertThat(transactions.rollbacks).isEqualTo(success ? 0 : 1);
    }

    @Test
    void blankQueriesAreEmptyAndCountsRemainLong() {
        assertThat(service.countRoleMenu(null)).isZero();
        assertThat(service.listRoleMenus((BigInteger) null)).isEmpty();
        assertThat(service.listRoleMenus(" ")).isEmpty();
        verifyNoInteractions(mapper);
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countRoleMenu(BigInteger.ONE)).isEqualTo(3000000000L);
    }

    @Test
    void stringLookupReturnsMenuIdsInMapperOrder() {
        when(mapper.list(any())).thenReturn(List.of(row(101, 2), row(102, 1)));
        assertThat(service.listRoleMenus("10")).containsExactly(BigInteger.TWO, BigInteger.ONE);
    }

    @Test
    void deleteRequiresScopeAndRetainsBothRoleAndMenuConstraints() {
        assertThatThrownBy(() -> service.deleteRoleMenu(null, null, "editor")).hasMessage("参数信息不能为空");
        service.deleteRoleMenu(roleId, BigInteger.ONE, "editor");
        ArgumentCaptor<Wrapper<RoleMenuDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<RoleMenuDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("role_id", "menu_id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(roleId, BigInteger.ONE, true, "editor");
    }
}
