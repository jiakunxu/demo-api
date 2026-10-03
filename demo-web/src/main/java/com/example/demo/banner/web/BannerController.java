package com.example.demo.banner.web;

import com.example.demo.banner.api.BannerService;
import com.example.demo.banner.api.bo.Banner;
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
@RequestMapping(value = "/banner")
public class BannerController extends BaseController {

    @Autowired
    private BannerService bannerService;

    @PreAuthorize("hasAuthority('banner:crud')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<Banner> list(HttpServletRequest request, HttpServletResponse response) {
        Banner banner = this.getParameter(request, new Banner());

        long count = bannerService.countBanner(banner);

        if (count == 0) {
            return new ListResponse<>(0, null);
        }

        return new ListResponse<>(count, bannerService.listBanners(banner));
    }

    @PreAuthorize("hasAuthority('banner:crud')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<Banner> get(HttpServletRequest request, HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        return new ObjectResponse<>(bannerService.getBanner(id));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('banner:crud')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ObjectResponse<Banner> save(HttpServletRequest request, HttpServletResponse response) {
        Banner banner = this.getParameter(request, Banner.class);
        return new ObjectResponse<>(bannerService.insertBanner(banner, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('banner:crud')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ObjectResponse<Banner> update(HttpServletRequest request, HttpServletResponse response) {
        Banner banner = this.getParameter(request, Banner.class);
        return new ObjectResponse<>(
            bannerService.updateBanner(banner.getId(), banner, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('banner:crud')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ObjectResponse<Banner> delete(HttpServletRequest request, HttpServletResponse response) {
        Banner banner = this.getParameter(request, Banner.class);
        return new ObjectResponse<>(
            bannerService.deleteBanner(banner.getId(), this.getUser().getName()));
    }

}
