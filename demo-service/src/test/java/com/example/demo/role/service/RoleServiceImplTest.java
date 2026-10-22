package com.example.demo.role.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.role.api.RoleMenuService;
import com.example.demo.role.api.bo.*;
import com.example.demo.role.dao.dataobject.RoleDO;
import com.example.demo.role.dao.mapper.RoleMapper;
import com.example.demo.user.api.UserRoleService;
import com.example.demo.cache.api.RedisService;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final RoleMapper mapper = mock(RoleMapper.class);
    private final RoleMenuService menus = mock(RoleMenuService.class);
    private final UserRoleService users = mock(UserRoleService.class);
    private final RedisService<String, Role> redis = mock(RedisService.class);
    private final RoleServiceImpl service = spy(new RoleServiceImpl());
    private final String key = RedisService.CACHE_KEY_ROLE + "10";
    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, RoleDO.class);
        ReflectionTestUtils.setField(service, "redisService", redis);
        ReflectionTestUtils.setField(service, "roleMenuService", menus);
        ReflectionTestUtils.setField(service, "userRoleService", users);
    }

    @Test
    void listsAllRolesWithoutArbitraryPaginationAndCountsRemainLong() {
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countRole(new Role())).isEqualTo(3000000000L);
        RoleDO row = new RoleDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listRoles()).extracting(Role::getId).containsExactly(id);
        verify(mapper).list(argThat(query -> query.getPageNo() == null && query.getPageSize() == null));
        assertThat(service.countRole(null)).isZero(); assertThat(service.listRoles((Role) null)).isEmpty();
        assertThat(service.getRole(" ")).isNull(); assertThat(service.getRoleId(" ")).isNull();
    }

    @Test
    void emptyRoleIdsAvoidDatabaseQuery() {
        assertThat(service.listRoles((List<BigInteger>) null)).isEmpty();
        assertThat(service.listRoles(List.<BigInteger>of())).isEmpty();
        verifyNoInteractions(mapper, redis);
    }

    @Test
    void roleIdsAreQueriedInOneBatchAndMappedToBusinessObjects() {
        List<BigInteger> ids = List.of(id, BigInteger.ONE);
        RoleDO row = new RoleDO(id);
        row.setName("管理员");
        when(mapper.selectByIds(ids)).thenReturn(List.of(row));

        List<Role> roles = service.listRoles(ids);

        assertThat(roles).extracting(Role::getId).containsExactly(id);
        assertThat(roles.getFirst().getName()).isEqualTo("管理员");
        verify(mapper).selectByIds(ids);
        verifyNoMoreInteractions(mapper);
        verifyNoInteractions(redis);
    }

    @Test
    void missingRoleIdsReturnEmptyList() {
        List<BigInteger> ids = List.of(id);
        when(mapper.selectByIds(ids)).thenReturn(List.of());

        assertThat(service.listRoles(ids)).isEmpty();
        verify(mapper).selectByIds(ids);
    }

    @Test
    void roleCodeAndStringIdLookupsMapIdentifiers() {
        RoleDO row = new RoleDO(); row.setId(id);
        doReturn(row).when(service).getById(id); doReturn(row).when(service).getOne(any(Wrapper.class));
        assertThat(service.getRole("10").getId()).isEqualTo(id);
        assertThat(service.getRoleId("admin")).isEqualTo(id);
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getRoleId("missing")).isNull();
    }

    @Test
    void cachedRoleAvoidsDatabaseAndMissingRoleIsRejected() {
        Role cached = new Role(); when(redis.get(key)).thenReturn(cached);
        assertThat(service.getRole(id)).isSameAs(cached);
        verify(service, never()).getById(any());
        when(redis.get(key)).thenReturn(null);
        doReturn(null).when(service).getById(id);
        assertThatThrownBy(() -> service.getRole(id)).hasMessage("角色不存在");
    }

    @Test
    void redisFailuresDoNotHideDatabaseRole() {
        when(redis.get(key)).thenThrow(new ServiceException(503, "offline"));
        RoleDO row = new RoleDO(); row.setId(id); doReturn(row).when(service).getById(id);
        doThrow(new ServiceException(503, "offline")).when(redis).set(eq(key), any());
        assertThat(service.getRole(id).getId()).isEqualTo(id);
    }

    @Test
    void insertionCreatesMenuRelationsWithGeneratedRoleId() {
        doAnswer(invocation -> { ((RoleDO) invocation.getArgument(0)).setId(id); return true; }).when(service).save(any(RoleDO.class));
        Role role = new Role(); role.setMenuIds(new BigInteger[] {BigInteger.ONE});
        assertThat(service.insertRole(role, "creator").getId()).isEqualTo(id);
        verify(menus).updateRoleMenus(id, role.getMenuIds(), "creator");
    }

    @Test
    void duplicateCodeRejectsInsertionBeforeMenuChanges() {
        doThrow(new DuplicateKeyException("duplicate")).when(service).save(any(RoleDO.class));
        assertThatThrownBy(() -> service.insertRole(new Role(), "creator")).hasMessage("编号已存在");
        verifyNoInteractions(menus);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void roleUpdatesOnlyChangeMenusAndCacheAfterSuccessfulWrite(boolean success) {
        doReturn(success).when(service).updateById(any(RoleDO.class));
        if (success) {
            service.updateRole(id, new Role(), "editor");
            verify(menus).updateRoleMenus(eq(id), isNull(), eq("editor"));
            verify(redis).remove(key);
        } else {
            assertThatThrownBy(() -> service.updateRole(id, new Role(), "editor")).isInstanceOf(ServiceException.class);
            verifyNoInteractions(menus, redis);
        }
    }

    @Test
    void statusUpdateClearsCacheEvenWhenInvalidationFails() {
        doReturn(true).when(service).update(any(Wrapper.class));
        doThrow(new RuntimeException("offline")).when(redis).remove(key);
        Role result = service.updateRole(id, "disabled", "editor");
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStatus()).isEqualTo("disabled");
        ArgumentCaptor<Wrapper<RoleDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<RoleDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id =");
        assertThat(wrapper.getSqlSet()).contains("status=", "update_by=")
            .doesNotContain("remark", "create_by", "create_time", "update_time");
        assertThat(wrapper.getParamNameValuePairs().values())
            .containsExactlyInAnyOrder(id, "disabled", "editor");
        verify(redis).remove(key);
        verifyNoInteractions(menus);
    }

    @Test
    void failedStatusUpdateDoesNotInvalidateCache() {
        doReturn(false).when(service).update(any(Wrapper.class));
        assertThatThrownBy(() -> service.updateRole(id, "disabled", "editor"))
            .isInstanceOf(ServiceException.class);
        verifyNoInteractions(menus, redis);
    }

    @Test
    void linkedUsersPreventDeletion() {
        when(users.countUserRole(id)).thenReturn(1L);
        assertThatThrownBy(() -> service.deleteRole(id, "editor")).hasMessage("已关联用户，请先调整用户角色");
        verify(service, never()).update(any(Wrapper.class)); verifyNoInteractions(menus, redis);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deletionOptionallyRemovesMenusAndInvalidatesCache(boolean hasMenus) {
        doReturn(true).when(service).update(any(Wrapper.class));
        when(menus.listRoleMenus(id)).thenReturn(hasMenus ? List.of(new RoleMenu()) : List.of());
        assertThat(service.deleteRole(id, "editor").getId()).isEqualTo(id);
        if (hasMenus) verify(menus).deleteRoleMenu(id, null, "editor");
        else verify(menus, never()).deleteRoleMenu(any(), any(), anyString());
        verify(redis).remove(key);
    }

}
