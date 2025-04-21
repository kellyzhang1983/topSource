package com.zkcompany.pojo;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name="tb_user_roles")
public class UserRole implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "created")
    private Date created;

    @Transient
    private String roleName;

    @Transient
    private String permissionName;
}
