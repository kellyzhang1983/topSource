package com.zkcompany.domain;

import com.zkcompany.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    private List<String> roleList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //创建一个类型为GrantedAuthority用户集合
        Collection<GrantedAuthority> userRole = new ArrayList<GrantedAuthority>();
        if(roleList == null || roleList.size() == 0){
           return userRole;
        }else{
            for(String roleString : roleList){
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(roleString);
                userRole.add(simpleGrantedAuthority);
            }

        }
        return userRole;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
