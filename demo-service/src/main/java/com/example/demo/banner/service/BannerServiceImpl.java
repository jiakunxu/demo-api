package com.example.demo.banner.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.banner.api.BannerService;
import com.example.demo.banner.api.bo.Banner;
import com.example.demo.banner.dao.dataobject.BannerDO;
import com.example.demo.banner.dao.mapper.BannerMapper;
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
public class BannerServiceImpl extends ServiceImpl<BannerMapper, BannerDO>
                               implements BannerService {

    @Override
    public long countBanner(Banner banner) {
        if (banner == null) {
            return 0;
        }

        return this.baseMapper.countBanner(BeanUtil.copy(banner, BannerDO.class));
    }

    @Override
    public List<Banner> listBanners(Banner banner) {
        if (banner == null) {
            return List.of();
        }

        List<Banner> list = BeanUtil
            .copy(this.baseMapper.listBanners(BeanUtil.copy(banner, BannerDO.class)), Banner.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public Banner getBanner(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        return getBanner(new BigInteger(id));
    }

    @Override
    public Banner getBanner(BigInteger id) {
        if (id == null) {
            return null;
        }

        return BeanUtil.copy(this.getById(id), Banner.class);
    }

    @Override
    public Banner insertBanner(@NotNull Banner banner, @NotBlank String creator) {
        BannerDO bannerDO = BeanUtil.copy(banner, BannerDO.class);
        bannerDO.setCreator(creator);

        this.save(bannerDO);

        banner.setId(bannerDO.getId());

        return banner;
    }

    @Override
    public Banner updateBanner(@NotNull BigInteger id, @NotNull Banner banner,
                               @NotBlank String modifier) {
        banner.setId(id);

        BannerDO bannerDO = BeanUtil.copy(banner, BannerDO.class);
        bannerDO.setModifier(modifier);

        if (!this.updateById(bannerDO)) {
            log.error("{}", bannerDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return banner;
    }

    @Override
    public Banner deleteBanner(@NotNull BigInteger id, @NotBlank String modifier) {
        var updateWrapper = Wrappers.<BannerDO> lambdaUpdate().eq(BannerDO::getId, id)
            .set(BannerDO::getDeleted, true).set(BannerDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{},{}", id, modifier);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return new Banner(id);
    }

}
