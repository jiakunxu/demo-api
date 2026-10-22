package com.example.demo.article.service;

import com.example.demo.article.api.bo.Article;
import com.example.demo.article.dao.dataobject.ArticleDO;
import com.example.demo.article.dao.mapper.ArticleMapper;
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

class ArticleServiceImplTest {
    private final BigInteger id = BigInteger.TEN;
    private final ArticleMapper mapper = mock(ArticleMapper.class);
    private final ArticleServiceImpl service = spy(new ArticleServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, ArticleDO.class); }

    @Test
    void missingQueryReturnsDefaultsWithoutDatabaseAccess() {
        assertThat(service.countArticle(null)).isZero();
        assertThat(service.listArticles(null)).isEmpty();
        assertThat(service.getArticle(" ")).isNull();
        assertThat(service.getArticle((BigInteger) null)).isNull();
        verifyNoInteractions(mapper);
    }

    @Test
    void countUsesLongAndPreservesSearchCriteria() {
        Article query = new Article(); query.setSearch("needle");
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countArticle(query)).isEqualTo(3000000000L);
        var capture = ArgumentCaptor.forClass(ArticleDO.class);
        verify(mapper).count(capture.capture());
        assertThat(capture.getValue().getSearch()).isEqualTo("needle");
    }

    @Test
    void listMapsIdentifiersAndPreservesPagination() {
        Article query = new Article(); query.setPageNo(2L); query.setPageSize(20L);
        ArticleDO row = new ArticleDO(); row.setId(id);
        when(mapper.list(any())).thenReturn(List.of(row));
        assertThat(service.listArticles(query)).extracting(Article::getId).containsExactly(id);
        var capture = ArgumentCaptor.forClass(ArticleDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingRowsProduceEmptyList(boolean nullList) {
        when(mapper.list(any())).thenReturn(nullList ? null : List.of());
        assertThat(service.listArticles(new Article())).isEmpty();
    }

    @Test
    void lookupByStringOrNumericIdMapsStoredRow() {
        ArticleDO row = new ArticleDO(); row.setId(id);
        doReturn(row).when(service).getById(id);
        assertThat(service.getArticle("10").getId()).isEqualTo(id);
        assertThat(service.getArticle(id).getId()).isEqualTo(id);
        doReturn(null).when(service).getById(id);
        assertThat(service.getArticle("10")).isNull();
    }

    @Test
    void insertionSetsCreatorAndReturnsGeneratedIdentifier() {
        doAnswer(invocation -> { ArticleDO row = invocation.getArgument(0); assertThat(row.getCreateBy()).isEqualTo("creator"); row.setId(id); return true; }).when(service).save(any(ArticleDO.class));
        Article entity = new Article();
        assertThat(service.insertArticle(entity, "creator")).isSameAs(entity);
        assertThat(entity.getId()).isEqualTo(id);
    }

    @Test
    void databaseInsertionFailurePropagates() {
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(service).save(any(ArticleDO.class));
        assertThatThrownBy(() -> service.insertArticle(new Article(), "creator")).isSameAs(failure);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateScopesIdentifierAndSetsModifierOrRejectsNoAffectedRows(boolean success) {
        doReturn(success).when(service).updateById(any(ArticleDO.class));
        Article changed = new Article();
        if (success) assertThat(service.updateArticle(id, changed, "editor")).isSameAs(changed);
        else assertThatThrownBy(() -> service.updateArticle(id, changed, "editor")).isInstanceOf(ServiceException.class);
        var capture = ArgumentCaptor.forClass(ArticleDO.class);
        verify(service).updateById(capture.capture());
        assertThat(capture.getValue().getId()).isEqualTo(id);
        assertThat(capture.getValue().getUpdateBy()).isEqualTo("editor");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteUsesLogicalFlagAndRequestedIdentifier(boolean success) {
        doReturn(success).when(service).update(any(Wrapper.class));
        if (success) assertThat(service.deleteArticle(id, "editor").getId()).isEqualTo(id);
        else assertThatThrownBy(() -> service.deleteArticle(id, "editor")).isInstanceOf(ServiceException.class);
        ArgumentCaptor<Wrapper<ArticleDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<ArticleDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id");
        assertThat(wrapper.getSqlSet()).contains("deleted", "update_by");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(id, true, "editor");
    }

}
