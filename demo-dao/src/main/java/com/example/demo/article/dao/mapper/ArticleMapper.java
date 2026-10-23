package com.example.demo.article.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.article.dao.dataobject.ArticleDO;

import java.util.List;

public interface ArticleMapper extends BaseMapper<ArticleDO> {

    long count(ArticleDO articleDO);

    List<ArticleDO> list(ArticleDO articleDO);

}
