package com.zkcompany.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Table(name="tb_user_points")
public class Point implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private String user_id;

    @Column(name = "points_change")
    private Integer points_change;

    @Column(name = "change_type")
    private int change_type;

    @Column(name = "change_time")
    private Date change_time;

    @Column(name = "points_detail")
    private String points_detail;

    /*public String getPoints_detail() {
        return points_detail;
    }

    public void setPoints_detail(String points_detail) {
        this.points_detail = points_detail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Integer getPoints_change() {
        return points_change;
    }

    public void setPoints_change(Integer points_change) {
        this.points_change = points_change;
    }

    public int getChange_type() {
        return change_type;
    }

    public void setChange_type(int change_type) {
        this.change_type = change_type;
    }

    public Date getChange_time() {
        return change_time;
    }

    public void setChange_time(Date change_time) {
        this.change_time = change_time;
    }*/


}
