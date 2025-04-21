package com.zkcompany.service.imp;

import com.zkcompany.dao.UserPointMapper;
import com.zkcompany.entity.DateUtil;
import com.zkcompany.entity.IdWorker;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.entity.WorldTime;
import com.zkcompany.pojo.Point;
import com.zkcompany.service.UserPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserPointServiceImpl  implements UserPointService {

    @Autowired
    private IdWorker idCreate;

    @Autowired
    private UserPointMapper userPointMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void addUserPoint(Map<String,Object> body) {
        //创建一个用户积分对象
        Point user_ponit = new Point();
        long nextId = idCreate.nextId();
        user_ponit.setUserId(body.get("user_id").toString());
        user_ponit.setId(String.valueOf(nextId));
        user_ponit.setPointsChange(Integer.valueOf(body.get("point").toString()));
        //user_ponit.setChange_type((int) (Math.random() * 3) + 1);
        user_ponit.setChangeType(Integer.valueOf(body.get("change_type").toString()));
        user_ponit.setPointsDetail(body.get("points_detail").toString());
        user_ponit.setChangeTime(WorldTime.chinese_time(new Date()));
        //使用mapper将用户积分对象的值插入数据库中
        userPointMapper.insertSelective(user_ponit);
    }

    /**
     *
     * @param user_id 用户ID，查询单个用户的积分
     * @return 积分的集合，一个用户ID包含多个积分明细
     */
    @Override
    public List<Point> findUserPoint(String user_id) {
        //从Redis中获取数据，数据结构Map<user_id,List<point>>
        List<Point> pointList = (List<Point>)redisTemplate.boundHashOps(SystemConstants.redis_userPoint).get(user_id);
        //如果Redis里没有数据，在数据库中查询数据
        if(pointList == null || pointList.size() == 0){
            Example example = new Example(Point.class);
            Example.Criteria criteria = example.createCriteria();
            //构建条件语句，相当于where user_id = ${user_id}
            criteria.andEqualTo("user_id", user_id);
            List<Point> points_mapper = userPointMapper.selectByExample(example);
            return points_mapper;
        }else{
            return pointList;
        }

    }

    /**
     *
     * @param user_id  用户ID
     * @return 用户总积分
     */
    @Override
    public Integer findUserTotalPoint(String user_id) {
        Integer totalPoint = 0;
        //从Redis中获取用户总分数
        List<Point> pointList = (List<Point>)redisTemplate.boundHashOps(SystemConstants.redis_userPoint).get(user_id);
        if(pointList == null || pointList.size() == 0){
            //从数据库查询用户总分数
            Point userTotalPoint = userPointMapper.findUserTotalPoint(user_id);
            if (userTotalPoint == null || "".equals(userTotalPoint)){
                return totalPoint;
            }else{
                return userTotalPoint.getPointsChange();
            }
        }else {
            //从Redis中把积分取出来，循环添加积分，得到用户总积分
            for(Point P : pointList){
                totalPoint = totalPoint + P.getPointsChange();
            }
            return  totalPoint;
        }
    }

    @Override
    public int cancelPonit(String user_id,String order_id) {

        Example example = new Example(Point.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("user_id",user_id);
        criteria.andEqualTo("points_detail",order_id);
        int i = userPointMapper.deleteByExample(example);
        return i;
    }


}
