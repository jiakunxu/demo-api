package com.example.demo.article.web;

import com.example.demo.article.api.ArticleService;
import com.example.demo.article.api.bo.Article;
import com.example.demo.framework.annotation.Log;
import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import com.example.demo.framework.web.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/article")
public class ArticleController extends BaseController {

    @Autowired
    private ArticleService articleService;

    @PreAuthorize("hasAuthority('article:crud')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<Article> list(HttpServletRequest request, HttpServletResponse response) {
        Article article = this.getParameter(request, new Article());

        long count = articleService.countArticle(article);

        if (count == 0) {
            return new ListResponse<>(0, null);
        }

        return new ListResponse<>(count, articleService.listArticles(article));
    }

    @PreAuthorize("hasAuthority('article:crud')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<Article> get(HttpServletRequest request, HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        return new ObjectResponse<>(articleService.getArticle(id));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('article:crud')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ObjectResponse<Article> save(HttpServletRequest request, HttpServletResponse response) {
        Article article = this.getParameter(request, Article.class);
        return new ObjectResponse<>(
            articleService.insertArticle(article, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('article:crud')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ObjectResponse<Article> update(HttpServletRequest request,
                                          HttpServletResponse response) {
        Article article = this.getParameter(request, Article.class);
        return new ObjectResponse<>(
            articleService.updateArticle(article.getId(), article, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('article:crud')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ObjectResponse<Article> delete(HttpServletRequest request,
                                          HttpServletResponse response) {
        Article article = this.getParameter(request, Article.class);
        return new ObjectResponse<>(
            articleService.deleteArticle(article.getId(), this.getUser().getName()));
    }

}
