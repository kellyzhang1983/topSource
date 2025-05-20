package com.zkcompany.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name="tb_user_points")
public class Point implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "points_change")
    private Integer pointsChange;

    @Column(name = "change_type")
    private int changeType;

    @Column(name = "change_time")
    private Date changeTime;

    @Column(name = "points_detail")
    private String pointsDetail;
}
