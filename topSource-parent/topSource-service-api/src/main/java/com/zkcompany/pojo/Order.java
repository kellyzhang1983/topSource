package com.zkcompany.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Table(name="tb_order")
public class Order implements Serializable {

    private static final long serialVersionUID = -7098443929702297887L;
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "order_date")
    private Date orderDate;

    @Column(name = "order_money")
    private BigDecimal orderMoney;

    @Column(name = "order_state")
    private String orderState;

    @Column(name = "order_activity")
    private String orderActivity;

    @Transient
    private String goodsId;

    @Transient
    private Integer grabStatus;

    @Transient
    private String desc;

    @Transient
    private List<OrderGoods> orderGoodsList = new ArrayList<OrderGoods>();

   /* public void setUser_id(String user_id) {
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

    public Date getOrder_date() {
        return order_date;
    }

    public void setOrder_date(Date order_date) {
        this.order_date = order_date;
    }

    public BigDecimal getOrder_money() {
        return order_money;
    }

    public void setOrder_money(BigDecimal order_money) {
        this.order_money = order_money;
    }

    public String getOrder_state() {
        return order_state;
    }

    public void setOrder_state(String order_state) {
        this.order_state = order_state;
    }*/


}
