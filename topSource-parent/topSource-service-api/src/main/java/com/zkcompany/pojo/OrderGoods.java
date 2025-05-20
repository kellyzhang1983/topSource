package com.zkcompany.pojo;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "tb_order_goods")
@Data
public class OrderGoods implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "order_id")
    private String orderId;

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

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "created")
    private Date created;

    @Column(name = "updated")
    private Date updated;

}
