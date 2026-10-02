package com.example.demo.article.api;

import com.example.demo.article.api.bo.Article;

import java.math.BigInteger;
import java.util.List;

public interface ArticleService {

    long countArticle(Article article);

    List<Article> listArticles(Article article);

    Article getArticle(String id);

    Article getArticle(BigInteger id);

    Article insertArticle(Article article, String creator);

    Article updateArticle(BigInteger id, Article article, String modifier);

    Article deleteArticle(BigInteger id, String modifier);

}
