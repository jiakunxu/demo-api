package com.example.demo.tree.api.bo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Tree implements Serializable {

    @Serial
    private static final long serialVersionUID = -3059859723083630131L;

    private BigInteger        id;

    /**
     * 指定节点标签为节点对象的某个属性值.
     */
    private String            label;

    /**
     * 指定子树为节点对象的某个属性值.
     */
    @ToString.Exclude
    private List<Tree>        children;

    /**
     * 指定节点选择框是否禁用为节点对象的某个属性值.
     */
    private Boolean           disabled;

    /**
     * 指定节点是否为叶子节点，仅在指定了 lazy 属性的情况下生效.
     */
    @JSONField(name = "isLeaf")
    private Boolean           leaf;

    /**
     * 自定义节点类名.
     */
    @JSONField(name = "class")
    private String            className;

}
