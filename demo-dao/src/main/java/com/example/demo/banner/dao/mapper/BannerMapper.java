package com.example.demo.banner.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.banner.dao.dataobject.BannerDO;

import java.util.List;

public interface BannerMapper extends BaseMapper<BannerDO> {

    int countBanner(BannerDO bannerDO);

    List<BannerDO> listBanners(BannerDO bannerDO);

}
