package com.zkcompany.pojo;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "tb_activity_goods")
public class ActivityGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "market_id")
    private String marketId;

    @Column(name = "goods_id")
    private String goodsId;

    @Column(name = "goods_name")
    private String goodsName;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "goods_image")
    private String goodsImage;

    @Column(name = "goods_num")
    private Integer goodsNum;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "created")
    private Date created;

}
