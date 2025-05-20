package com.zkcompany.pojo;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_market_activity")
public class MarketActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "activity_image")
    private String activityImage;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "begin_date")
    private Date beginDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "activity_status")
    private String activityStatus;

    @Column(name = "activity_desc")
    private String activityDesc;

    @Transient
    private List<ActivityGoods> activityGoodsList = new ArrayList<ActivityGoods>();
}
