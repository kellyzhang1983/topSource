package com.zkcompany.service.imp;

import com.zkcompany.dao.UserMapper;
import com.zkcompany.entity.*;
import com.zkcompany.pojo.User;
import com.zkcompany.service.UserService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;


import java.util.*;

/****
 * @Author:zk
 * @Description:OrderConfig业务层接口实现类
 * @Date
 *****/
@Service
public class UserServiceImpl implements UserService , UserDetailsService {
    @Autowired
    private IdWorker idCreate;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean addUser(String userName,String password,String Ip) {
        //生产唯一ID，用IdWork生成器
        String id = String.valueOf(idCreate.nextId());
        //生成用户正式姓名，RandomValueUtil这个公共类生产名字、性别、电话、邮箱
        String name = RandomValueUtil.getChineseName();
        String email = RandomValueUtil.getEmail(0,14);
        String telphone = RandomValueUtil.getTelephone();
        String name_sex = RandomValueUtil.name_sex;
        String sex = "1";
        if (name_sex.equals("女")){
            sex = "0";
        }
        //通过User实体类，SET进行注入User
        try {
            User user = new User();
            user.setId(id);
            user.setUsername(userName);
            user.setPassword(password);
            user.setIp(Ip);
            user.setCreated(new Date());
            user.setEmail(email);
            user.setPhone(telphone);
            user.setSex(sex);
            user.setLast_login_time(new Date());
            user.setLastUpdate(new Date());
            user.setName(name);
            user.setStatus("1");
            //用Mapper调用数据新增一条数据
            userMapper.insertSelective(user);
            //成功返回true ,失败返回false
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User findUser(String id) {
        //根据用户ID（主Key）查询用户信息
        User user = (User)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(id);
        if(user == null || user.equals("")){
            return userMapper.selectByPrimaryKey(id);
        }else {
            return user;
        }

    }

    @Override
    public Map<String, Object> findAllUserPage(Map<String,Object>  body) {
        //从Redis里面过去数据
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userInfo).keys();
        //申名查询出来的结果为List
        List<User> userList = null;
        //申名封装了分页结果数据位最终返回的Map
        Map<String, Object> resultMap = null;
        //首先判断Redis中有数据没有
        if (keys == null || keys.isEmpty()) {
            //如果传来的参数整体为空，直接在数据库中全量查询、不加任何条件
            if (body == null || body.isEmpty()) {
                userList = userMapper.selectAll();
            } else {
                //如果参数不为空，那么直接用条件构造器构造条件，然后数据进行查询
                Example example = createExample(User.class, body);
                userList = userMapper.selectByExample(example) ;
            }
        } else {
            //判断Redis中有数据，那么直接判断
            userList = new ArrayList<User>();
            //如果传来的参数整体为空，全量查询Redis中的数据
            if (body == null || body.isEmpty()) {
                for (Object key : keys) {
                    //循坏读取Redis里面的数据，用Key循环找出值
                    User user = (User) redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(key);
                    //并把数据加入结果列表里面
                    userList.add(user);
                }
            } else {
                for (Object key : keys) {
                    User user = (User) redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(key);
                    //把数据读取出来每个进行进行对比，如果返回false，舍弃掉，不加入结果列表里面
                    Boolean example = createExample(body, key, user);
                    if (example) {
                        userList.add(user);
                    }
                }
            }
        }

        try {
            //判断传过来的参数是否有分页信息
            if(body == null || body.isEmpty()){
                resultMap = PageHelp.PageList(userList);
            }else {
                if(!StringUtils.isEmpty(body.get("currentPage") == null ? "" : body.get("currentPage").toString())
                        && !StringUtils.isEmpty(body.get("pageSize") == null ? "": body.get("pageSize").toString())){
                    //如果有进行强转
                    int currentPage = (Integer) body.get("currentPage");
                    int pageSize = (Integer) body.get("pageSize");
                    //检查CurrentPage，pageSize是否小于、等于0，如果有，那么初始一个默认值
                    Map<String, Integer> pageInfo = PageHelp.decidePage(currentPage, pageSize);
                    currentPage = pageInfo.get("page");
                    pageSize = pageInfo.get("pageSize");
                    //用PageHelp把userList进行分页操作
                    List<User> resultList = PageHelp.paginate(userList, currentPage, pageSize);
                    //用PageHelp封装分页信息
                    resultMap = PageHelp.PageList(resultList, currentPage, pageSize, userList.size());
                }else {
                    resultMap = PageHelp.PageList(userList);
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        return resultMap;
    }



    @Override
    public int deleteUser(String id){
        int i = userMapper.deleteByPrimaryKey(id);
        return i;
    }

    @Override
    public List<User> findUserState(String status) {
        Set<String> userKeyList = (Set<String>)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).keys();
        List<User> userList = null;
        if(userKeyList == null || userKeyList.size() == 0){
            return userMapper.findUserState(status);
        }else {
            userList = new ArrayList<User>();
            for(String Key: userKeyList){
              User user =  (User)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(Key);
              if(status.equals(user.getStatus())){
                  userList.add(user);
              }
            }
            return userList;
        }
    }

    @Override
    public User findUserByUserName(String userName) {
        User user = (User)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(userName);
        if(user == null || user.equals("")){
            Example example = new Example(User.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("username",userName);
            List<User> users = userMapper.selectByExample(example);
            if(users == null || users.size() == 0 ){
                return null;
            }else{
                return users.get(0);
            }
        }else {
            return user;
        }
    }

    private Example createExample(Class userClass,Map<String,Object> body){
        //创建条件构造器，并告知Example这个条件User这个对象的条件
        Example example = new Example(userClass,true,true);
        Example.Criteria criteria = example.createCriteria();

        Object id = body.get("id");
        if (!StringUtils.isEmpty(id == null ? "" : id.toString())){
            //相当于”select id from table_name where id = XXXX?“
            criteria.andEqualTo("id",id);
        }
        Object username = body.get("username");
        if (!StringUtils.isEmpty(username == null ? "" : username.toString())){
            //相当于”select id from table_name where id = XXXX and username like %xxxx%"
            criteria.andLike("username","%" + username + "%");
        }
        Object phone = body.get("phone");
        if (!StringUtils.isEmpty(phone == null ? "" : phone.toString())){
            criteria.andLike("phone","%" + phone + "%");
        }
        Object created = body.get("created");
        if (!StringUtils.isEmpty(created == null ? "" : created.toString())){
            //相当于”select id from table_name where id = XXXX and username like %xxxx% and created >= XXXXX"
            criteria.andGreaterThanOrEqualTo("created",created);
        }
        Object source_type = body.get("source_type");
        if (!StringUtils.isEmpty(source_type == null ? "" : source_type.toString())){
            criteria.andEqualTo("source_type",source_type);
        }
        Object nick_name = body.get("nick_name");
        if (!StringUtils.isEmpty(nick_name == null ? "" : nick_name.toString())){
            criteria.andLike("nick_name","%" + nick_name + "%");
        }
        Object  name = body.get("name");
        if (!StringUtils.isEmpty(name == null ? "" : name.toString())){
            criteria.andLike("name","%" + name + "%");
        }
        Object status = body.get("status");
        if (!StringUtils.isEmpty(status == null ? "" : status.toString())){
            criteria.andEqualTo("status",status);
        }
        Object sex = body.get("sex");
        if (!StringUtils.isEmpty(sex == null ? "" : sex.toString())){
            criteria.andEqualTo("sex",sex);
        }
        Object last_login_time = body.get("last_login_time");
        if (!StringUtils.isEmpty(last_login_time == null ? "" : last_login_time.toString())){
            criteria.andGreaterThanOrEqualTo("last_login_time",last_login_time);
        }
        return example;
    }

    private Boolean createExample(Map<String,Object> body,Object key,User user){
        Boolean Example = true;
        Object id = body.get("id");
        if(!StringUtils.isEmpty(id == null ? "" : id.toString())){
            //参数传过来的数据与对象里面的id做比较，如果一样，比对下个字段，如果不一样，立马返回false，数据不加入结果数据List中
            if(id.equals(user.getId())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }
        Object username = body.get("username");
        if(!StringUtils.isEmpty(username == null ? "" : username.toString())){
            //参数传过来的数据与对象里面的username做比较，如果传过来的数据被包含在对象的username里面，则继续比对下个字段，如果不一样，立马返回false，数据不加入结果数据List中
            //ystem.out.println(user.getUsername().contains(username.toString()));
            if (user.getUsername().contains(username.toString())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object phone = body.get("phone");
        if(!StringUtils.isEmpty(phone == null ? "" : phone.toString())){
            if (user.getPhone().contains(phone.toString())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object created = body.get("created");
        if (!StringUtils.isEmpty(created == null ? "" : created.toString())){
            Date formatDate = DateUtil.formatStr(created.toString(),DateUtil.PATTERN_YYYY_MM_DDHHMMSS);
            int result = formatDate.compareTo(user.getCreated());
            //参数传过来的数据与对象里面的created做比较，如果created < user.getCreated() ，则继续比对下个字段,立马返回false，数据不加入结果数据List中
            if (result <= 0){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object source_type = body.get("source_type");
        if (!StringUtils.isEmpty(source_type == null ? "" : source_type.toString())){
            if(source_type.equals(user.getSource_type())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object nick_name = body.get("nick_name");
        if (!StringUtils.isEmpty(nick_name == null ? "" : nick_name.toString())){
            if (user.getNick_name().contains(nick_name.toString())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object name = body.get("name");
        if (!StringUtils.isEmpty(name == null ? "" : name.toString())){
            if (user.getName().contains(name.toString())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object status = body.get("status");
        if (!StringUtils.isEmpty(status == null ? "" : status.toString())){
            if(status.equals(user.getStatus())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object sex = body.get("sex");
        if (!StringUtils.isEmpty(sex == null ? "" : sex.toString())){
            if(sex.equals(user.getSex())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object last_login_time = body.get("last_login_time");
        if (!StringUtils.isEmpty(last_login_time == null ? "" : last_login_time.toString())){
            Date formatDate = DateUtil.formatStr(last_login_time.toString(),DateUtil.PATTERN_YYYY_MM_DDHHMMSS);
            int result = formatDate.compareTo(user.getLast_login_time());
            if (result <= 0){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }
        return Example;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
