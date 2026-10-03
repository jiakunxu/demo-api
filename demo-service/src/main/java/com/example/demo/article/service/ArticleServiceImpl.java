package com.example.demo.article.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.article.api.ArticleService;
import com.example.demo.article.api.bo.Article;
import com.example.demo.article.dao.dataobject.ArticleDO;
import com.example.demo.article.dao.mapper.ArticleMapper;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticleDO>
                                implements ArticleService {

    @Override
    public long countArticle(Article article) {
        if (article == null) {
            return 0;
        }

        return this.baseMapper.countArticle(BeanUtil.copy(article, ArticleDO.class));
    }

    @Override
    public List<Article> listArticles(Article article) {
        if (article == null) {
            return List.of();
        }

        List<Article> list = BeanUtil.copy(
            this.baseMapper.listArticles(BeanUtil.copy(article, ArticleDO.class)), Article.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public Article getArticle(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        return getArticle(new BigInteger(id));
    }

    @Override
    public Article getArticle(BigInteger id) {
        if (id == null) {
            return null;
        }

        return BeanUtil.copy(this.getById(id), Article.class);
    }

    @Override
    public Article insertArticle(@NotNull Article article, @NotBlank String creator) {
        ArticleDO articleDO = BeanUtil.copy(article, ArticleDO.class);
        articleDO.setCreator(creator);

        this.save(articleDO);

        article.setId(articleDO.getId());

        return article;
    }

    @Override
    public Article updateArticle(@NotNull BigInteger id, @NotNull Article article,
                                 @NotBlank String modifier) {
        article.setId(id);

        ArticleDO articleDO = BeanUtil.copy(article, ArticleDO.class);
        articleDO.setModifier(modifier);

        if (!this.updateById(articleDO)) {
            log.error("{}", articleDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return article;
    }

    @Override
    public Article deleteArticle(@NotNull BigInteger id, @NotBlank String modifier) {
        var updateWrapper = Wrappers.<ArticleDO> lambdaUpdate().eq(ArticleDO::getId, id)
            .set(ArticleDO::getDeleted, true).set(ArticleDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{},{}", id, modifier);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return new Article(id);
    }

}
