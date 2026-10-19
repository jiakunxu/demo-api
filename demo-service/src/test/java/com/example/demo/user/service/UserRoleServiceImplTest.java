package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.user.api.UserService;
import com.example.demo.user.api.bo.*;
import com.example.demo.user.dao.dataobject.*;
import com.example.demo.user.dao.mapper.UserRoleMapper;
import com.example.demo.role.api.RoleService;
import com.example.demo.role.api.bo.Role;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import com.example.demo.support.TransactionProbe;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRoleServiceImplTest {
    private final BigInteger corp = BigInteger.valueOf(20);
    private final BigInteger user = BigInteger.TEN;
    private final UserService users = mock(UserService.class);
    private final RoleService roles = mock(RoleService.class);
    private final UserRoleMapper mapper = mock(UserRoleMapper.class);
    private final UserRoleServiceImpl service = spy(new UserRoleServiceImpl());
    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, UserRoleDO.class);
        ReflectionTestUtils.setField(service, "userService", users);
        ReflectionTestUtils.setField(service, "roleService", roles);
        doReturn(true).when(service).saveBatch(anyCollection());
        doReturn(true).when(service).update(any(Wrapper.class));
    }
    private UserRoleDO row(int id, int role) {
        var row = new UserRoleDO();
        row.setId(BigInteger.valueOf(id));
        row.setUserId(user);
        row.setRoleId(BigInteger.valueOf(role));
        return row;
    }

    @Test
    void differenceUpdateValidatesUserAndRefreshesPermissionsAfterWrites() {
        when(mapper.listUserRoles(any())).thenReturn(List.of(row(101, 1), row(102, 2)));
        service.updateUserRole(corp, user, new String[] {"2", "3"}, "editor");
        ArgumentCaptor<Collection<UserRoleDO>> batch = ArgumentCaptor.captor();
        verify(service).saveBatch(batch.capture());
        assertThat(batch.getValue()).hasSize(1);
        var added = batch.getValue().iterator().next();
        assertThat(added.getUserId()).isEqualTo(user);
        assertThat(added.getRoleId()).isEqualTo(BigInteger.valueOf(3));
        assertThat(added.getCreateBy()).isEqualTo("editor");
        ArgumentCaptor<Wrapper<UserRoleDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<UserRoleDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(BigInteger.valueOf(101), true, "editor").doesNotContain(BigInteger.valueOf(102));
        var sequence = inOrder(users, mapper, service);
        sequence.verify(users).validate(corp, user);
        sequence.verify(mapper).listUserRoles(any());
        sequence.verify(service).saveBatch(anyCollection());
        sequence.verify(service).update(any(Wrapper.class));
        sequence.verify(users).refreshToken(corp, user, "editor");
    }

    @Test
    void unchangedRolesDoNotWriteButStillRefreshToken() {
        when(mapper.listUserRoles(any())).thenReturn(List.of(row(101, 1)));
        service.updateUserRole(corp, user, new String[] {"1"}, "editor");
        verify(service, never()).saveBatch(anyCollection());
        verify(service, never()).update(any(Wrapper.class));
        verify(users).refreshToken(corp, user, "editor");
    }

    @Test
    void unauthorizedUserCannotQueryOrChangeRelations() {
        doThrow(new ServiceException(500, "暂无权限")).when(users).validate(corp, user);
        assertThatThrownBy(() -> service.updateUserRole(corp, user, new String[] {"1"}, "editor")).hasMessage("暂无权限");
        verifyNoInteractions(mapper);
        verify(service, never()).saveBatch(anyCollection());
        verify(users, never()).refreshToken(any(), any(BigInteger.class), anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void initialAssignmentAndClearingHaveDifferentWritePaths(boolean existing) {
        if (existing) when(mapper.listUserRoles(any())).thenReturn(List.of(row(101, 1)));
        service.updateUserRole(corp, user, existing ? null : new String[] {"1", "2"}, "editor");
        if (existing) { verify(service).update(any(Wrapper.class));
        verify(service, never()).saveBatch(anyCollection());
        }
        else { verify(service).saveBatch(anyCollection()); verify(service, never()).update(any(Wrapper.class)); }
        verify(users).refreshToken(corp, user, "editor");
    }

    @Test
    void failedDeletionRollsBackThroughSpringAdviceAndDoesNotRefreshToken() {
        when(mapper.listUserRoles(any())).thenReturn(List.of(row(101, 1)));
        doReturn(false).when(service).update(any(Wrapper.class));
        var transactions = new TransactionProbe();
        assertThatThrownBy(() -> transactions.wrap(service).updateUserRole(corp, user, null, "editor")).isInstanceOf(ServiceException.class);
        assertThat(transactions.rollbacks).isEqualTo(1);
        assertThat(transactions.commits).isZero();
        verify(users, never()).refreshToken(any(), any(BigInteger.class), anyString());
    }

    @Test
    void assigningOneRoleToMultipleUsersUsesOneBatchAndReturnsIds() {
        doAnswer(invocation -> {
            Collection<UserRoleDO> batch = invocation.getArgument(0);
            batch.forEach(row -> row.setId(row.getUserId().add(BigInteger.valueOf(100)))); return true;
        }).when(service).saveBatch(anyCollection());
        List<UserRole> result = service.updateUserRole(corp, new String[] {"10", "11"}, BigInteger.ONE, "editor");
        assertThat(result).extracting(UserRole::getId).containsExactly(BigInteger.valueOf(110), BigInteger.valueOf(111));
        verify(service).saveBatch(anyCollection());
        verify(users).validate(corp, "10"); verify(users).validate(corp, "11");
        verify(users).refreshToken(corp, user, "editor");
        verify(users).refreshToken(corp, BigInteger.valueOf(11), "editor");
    }

    @Test
    void batchAuthorizationFailurePreventsAllWrites() {
        doThrow(new ServiceException(500, "暂无权限")).when(users).validate(corp, "11");
        assertThatThrownBy(() -> service.updateUserRole(corp, new String[] {"10", "11"}, BigInteger.ONE, "editor")).hasMessage("暂无权限");
        verify(service, never()).saveBatch(anyCollection());
        verify(users, never()).refreshToken(any(), any(BigInteger.class), anyString());
    }

    @Test
    void deletingSingleAndBatchRelationsUsesRequestedScope() {
        service.deleteUserRole(corp, user, BigInteger.ONE, "editor");
        service.deleteUserRole(corp, new String[] {"10", "11"}, BigInteger.ONE, "editor");
        ArgumentCaptor<Wrapper<UserRoleDO>> capture = ArgumentCaptor.captor();
        verify(service, times(2)).update(capture.capture());
        for (Wrapper<UserRoleDO> value : capture.getAllValues()) {
            var wrapper = (LambdaUpdateWrapper<UserRoleDO>) value;
            assertThat(wrapper.getSqlSegment()).contains("user_id", "role_id");
            assertThat(wrapper.getParamNameValuePairs().values()).contains(BigInteger.ONE, user, true, "editor");
        }
        verify(users).refreshToken(eq(corp), org.mockito.AdditionalMatchers.aryEq(new BigInteger[] {user, BigInteger.valueOf(11)}), eq("editor"));
    }

    @Test
    void emptyUserSelectionIsNoOp() {
        assertThat(service.updateUserRole(corp, new String[0], BigInteger.ONE, "editor")).isEmpty();
        assertThat(service.deleteUserRole(corp, new String[0], BigInteger.ONE, "editor").getRoleId()).isEqualTo(BigInteger.ONE);
        verifyNoInteractions(users, mapper);
        verify(service, never()).saveBatch(anyCollection());
        verify(service, never()).update(any(Wrapper.class));
    }

    @Test
    void countsSupportLargeValuesAndIgnoreMissingCriteria() {
        assertThat(service.countUserRole((BigInteger) null)).isZero();
        assertThat(service.countUserRole(user, " ")).isZero();
        assertThat(service.countUserRole(user, new String[0])).isZero();
        verifyNoInteractions(mapper);
        when(mapper.countUserRole(any())).thenReturn(3000000000L);
        assertThat(service.countUserRole(BigInteger.ONE)).isEqualTo(3000000000L);
        assertThat(service.countUserRole(user, "admin")).isEqualTo(3000000000L);
        assertThat(service.countUserRole(user, new String[] {"admin", "user"})).isEqualTo(3000000000L);
    }

    @Test
    void deduplicatesRoleIdsAndReturnsBatchResultWithRequestedStatus() {
        when(mapper.listUserRoles(any()))
            .thenReturn(List.of(row(101, 1), row(102, 2), row(103, 1)));
        Role first = new Role(); Role second = new Role();
        first.setId(BigInteger.ONE);
        second.setId(BigInteger.TWO);
        List<BigInteger> ids = List.of(BigInteger.ONE, BigInteger.TWO);
        when(roles.listRoles(ids)).thenReturn(List.of(second, first));
        assertThat(service.listRoles(user, "enable")).containsExactly(second, first);
        verify(roles).listRoles(ids);
        verifyNoMoreInteractions(roles);
        var capture = ArgumentCaptor.forClass(UserRoleDO.class);
        verify(mapper).listUserRoles(capture.capture());
        assertThat(capture.getValue().getUserId()).isEqualTo(user);
        assertThat(capture.getValue().getStatus()).isEqualTo("enable");
        assertThat(service.listUserRoles("10")).hasSize(3);
    }

    @Test
    void missingUserOrRelationsAvoidRoleQueries() {
        assertThat(service.listRoles(null, "enable")).isEmpty();
        verifyNoInteractions(mapper, roles);
        when(mapper.listUserRoles(any())).thenReturn(List.of());
        assertThat(service.listRoles(user, "enable")).isEmpty();
        verifyNoInteractions(roles);
    }

    @Test
    void missingRoleInBatchReturnsOnlyExistingRoles() {
        when(mapper.listUserRoles(any())).thenReturn(List.of(row(101, 1), row(102, 2)));
        Role first = new Role();
        first.setId(BigInteger.ONE);
        when(roles.listRoles(List.of(BigInteger.ONE, BigInteger.TWO)))
            .thenReturn(List.of(first));

        assertThat(service.listRoles(user, "enable")).containsExactly(first);
        verify(roles).listRoles(List.of(BigInteger.ONE, BigInteger.TWO));
        verifyNoMoreInteractions(roles);
    }

    @Test
    void codeBasedUserListingUsesCountAsPageSizeAndEnabledExistenceFilters() {
        when(mapper.countUser(any())).thenReturn(3000000000L);
        UserDO row = new UserDO(); row.setId(user);
        when(mapper.listUsers(any())).thenReturn(List.of(row));
        assertThat(service.listUsers(corp, "admin", "user")).extracting(User::getId).containsExactly(user);
        var capture = ArgumentCaptor.forClass(UserDO.class);
        verify(mapper).listUsers(capture.capture());
        var query = capture.getValue();
        assertThat(query.getPageSize()).isEqualTo(3000000000L);
        assertThat(query.getEnabled()).isTrue(); assertThat(query.getExists()).isTrue();
        assertThat(query.getCorpId()).isEqualTo(corp);
        assertThat(query.getCodes()).containsExactly("admin", "user");
    }

    @Test
    void roleBasedUserQueriesPreserveCorpAndExistenceCriteria() {
        when(mapper.countUser(any())).thenReturn(2L);
        assertThat(service.countUser(corp, "1", "true", new User())).isEqualTo(2L);
        service.listUsers(corp, "1", "false", new User());
        var capture = ArgumentCaptor.forClass(UserDO.class);
        verify(mapper).listUsers(capture.capture());
        assertThat(capture.getValue().getCorpId()).isEqualTo(corp);
        assertThat(capture.getValue().getRoleId()).isEqualTo(BigInteger.ONE);
        assertThat(capture.getValue().getExists()).isFalse();
    }

    @Test
    void individualInsertionReturnsGeneratedIdentifier() {
        doAnswer(invocation -> { ((UserRoleDO) invocation.getArgument(0)).setId(BigInteger.valueOf(99)); return true; }).when(service).save(any(UserRoleDO.class));
        assertThat(service.insertUserRole(user, BigInteger.ONE, "creator").getId()).isEqualTo(BigInteger.valueOf(99));
    }
}
