package com.example.demo.menu.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.demo.menu.api.bo.Menu;
import com.example.demo.menu.dao.dataobject.MenuDO;
import com.example.demo.menu.dao.mapper.MenuMapper;
import com.example.demo.role.api.RoleMenuService;
import com.example.demo.tree.api.bo.Tree;
import com.example.demo.support.ServiceTestSupport;
import com.example.demo.framework.exception.ServiceException;
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

class MenuServiceImplTest {
    private final MenuMapper mapper = mock(MenuMapper.class);
    private final RoleMenuService roles = mock(RoleMenuService.class);
    private final MenuServiceImpl service = spy(new MenuServiceImpl());
    private final BigInteger id = BigInteger.TEN;
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, MenuDO.class);
    ReflectionTestUtils.setField(service, "roleMenuService", roles);
    }
    private MenuDO row(int id, int pid) { MenuDO menu = new MenuDO();
    menu.setId(BigInteger.valueOf(id));
    menu.setPid(BigInteger.valueOf(pid));
    menu.setName("menu" + id);
    return menu;
    }

    @Test
    void treeBuildsNestedChildrenAndFiltersRequestedIdentifiers() {
        when(mapper.list(any())).thenReturn(List.of(row(1, 0), row(2, 1), row(3, 2), row(4, 0)));
        List<Tree> trees = service.listMenus(List.of(BigInteger.ONE, BigInteger.TWO));
        assertThat(trees).hasSize(2);
        assertThat(trees.getFirst().getChildren()).hasSize(1);
        assertThat(trees.getFirst().getChildren().getFirst().getChildren()).hasSize(1);
        verify(mapper).list(argThat(query -> query.getIds().equals(List.of(BigInteger.ONE, BigInteger.TWO))));
        assertThat(service.listMenus()).hasSize(2);
    }

    @Test
    void typeQueriesUseEnabledStatusAndOptionalUserScope() {
        service.listMenus(new String[] {"menu"}); service.listMenus(new String[] {"button"}, id);
        var capture = ArgumentCaptor.forClass(MenuDO.class);
        verify(mapper, times(2)).list(capture.capture());
        assertThat(capture.getAllValues()).extracting(MenuDO::getStatus).containsOnly(Menu.Status.ENABLE.value);
        assertThat(capture.getAllValues().get(1).getUserId()).isEqualTo(id);
    }

    @Test
    void parentAndNameQueriesPreserveFiltersAndCountsRemainLong() {
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countMenu(id)).isEqualTo(3000000000L);
        assertThat(service.countMenu("10", new Menu())).isEqualTo(3000000000L);
        service.listMenus("10", new Menu()); service.listMenus("needle", "enable");
        var capture = ArgumentCaptor.forClass(MenuDO.class); verify(mapper, times(2)).list(capture.capture());
        assertThat(capture.getAllValues().getFirst().getPid()).isEqualTo(id);
        assertThat(capture.getAllValues().get(1).getName()).isEqualTo("needle");
        assertThat(capture.getAllValues().get(1).getStatus()).isEqualTo("enable");
    }

    @Test
    void missingQueriesReturnDefaults() {
        assertThat(service.countMenu((BigInteger) null)).isZero(); assertThat(service.countMenu("", null)).isZero();
        assertThat(service.listMenus("", (Menu) null)).isNull(); assertThat(service.listMenus(new String[0])).isNull();
        assertThat(service.listMenus(new String[] {"menu"}, null)).isNull();
        assertThat(service.getMenu(" ")).isNull(); assertThat(service.getMenu((BigInteger) null)).isNull();
        verifyNoInteractions(mapper);
        assertThat(service.listMenus()).isNull();
    }

    @Test
    void lookupAndInsertReturnStoredAndGeneratedIdentifiers() {
        MenuDO stored = row(10, 0); doReturn(stored).when(service).getById(id);
        assertThat(service.getMenu("10").getId()).isEqualTo(id); assertThat(service.getMenu(id).getId()).isEqualTo(id);
        doAnswer(invocation -> { MenuDO row = invocation.getArgument(0); assertThat(row.getPid()).isEqualTo(BigInteger.ONE); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(MenuDO.class));
        assertThat(service.insertMenu(BigInteger.ONE, new Menu(), "creator").getId()).isEqualTo(id);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void failedUpdateIsRejected(boolean success) {
        doReturn(success).when(service).updateById(any(MenuDO.class));
        if (success) assertThat(service.updateMenu(id, new Menu(), "editor").getId()).isEqualTo(id);
        else assertThatThrownBy(() -> service.updateMenu(id, new Menu(), "editor")).isInstanceOf(ServiceException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"children", "roles", "none", "write failed"})
    void deletionRejectsReferencedMenusAndFailedWrites(String reason) {
        when(mapper.count(any())).thenReturn(reason.equals("children") ? 1L : 0L);
        when(roles.countRoleMenu(id)).thenReturn(reason.equals("roles") ? 1L : 0L);
        doReturn(reason.equals("none")).when(service).update(any(Wrapper.class));
        if (reason.equals("none")) assertThat(service.deleteMenu(id, "editor").getId()).isEqualTo(id);
        else assertThatThrownBy(() -> service.deleteMenu(id, "editor")).isInstanceOf(ServiceException.class);
        if (reason.equals("children") || reason.equals("roles")) verify(service, never()).update(any(Wrapper.class));
    }
}
