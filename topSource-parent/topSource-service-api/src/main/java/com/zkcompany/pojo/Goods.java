package com.zkcompany.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "tb_sku")
@Data
public class Goods implements Serializable {
    @Id
    @Column(name = "id")
    @JsonProperty("id")
    private String id;

    @Column(name = "name")
    @JsonProperty("name")
    private String name;

    @Column(name = "price")
    @JsonProperty("price")
    private BigDecimal price;

    @Column(name = "num")
    @JsonProperty("num")
    private Integer num;

    @Column(name = "alert_num")
    @JsonProperty("alert_num")
    private Integer alertNum;

    @Column(name = "image")
    @JsonProperty("image")
    private String image;

    @Column(name = "weight")
    @JsonProperty("weight")
    private Integer weight;

    @Column(name = "create_time")
    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    @Column(name = "update_time")
    @JsonProperty("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    @Column(name = "brand_name")
    @JsonProperty("brand_name")
    private String brandName;

    @Column(name = "spec")
    private String spec;

    @Column(name = "sale_num")
    @JsonProperty("sale_num")
    private Integer saleNum;

    @Column(name = "comment_num")
    @JsonProperty("comment_num")
    private Integer commentNum;

    @Column(name = "status")
    @JsonProperty("status")
    private String status;

    @Column(name = "isAddWeight")
    @JsonProperty("isAddWeight")
    private String isAddWeight;

    @Transient
    @JsonProperty("color")
    private String color;

    @Transient
    @JsonProperty("version")
    private String version;

    @Transient
    @JsonProperty("size")
    private String size;

    @Transient
    @JsonProperty("spec_search")
    private String spec_search;

    @Transient
    @JsonProperty("taste")
    private String taste;

    @Transient
    @JsonProperty("suggestion")
    private List<String> suggestion;


}
