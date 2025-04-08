package com.zkcompany.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Table(name = "tb_brand")
public class Brand implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "letter")
    private String letter;

    @Column(name = "seq")
    private Integer seq;
}
