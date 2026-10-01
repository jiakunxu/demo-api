package com.example.demo.banner.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_banner")
public class BannerDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -696823067103923860L;

    private BigInteger        id;

    public BannerDO(BigInteger id) {
        this.id = id;
    }

}
