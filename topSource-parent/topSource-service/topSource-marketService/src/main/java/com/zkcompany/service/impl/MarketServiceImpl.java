package com.zkcompany.service.impl;

import com.zkcompany.dao.ActivityGoodsMapper;
import com.zkcompany.dao.ActivityStatusMapper;
import com.zkcompany.dao.MarketActivityMapper;
import com.zkcompany.entity.*;
import com.zkcompany.pojo.*;
import com.zkcompany.service.MarketService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class MarketServiceImpl implements MarketService, UserDetailsService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MarketActivityMapper marketActivityMapper;

    @Autowired
    private ActivityGoodsMapper activityGoodsMapper;

    @Autowired
    private ActivityStatusMapper activityStatusMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Transactional
    @Override
    public String addMarketActivity(MarketActivity marketActivity, MultipartFile activityImage, List<ActivityGoods> activityGoodsList) throws Exception {
        //给文件重新命名
        String name = "activity_image_" + System.currentTimeMillis();
        int lastIndexOf = activityImage.getOriginalFilename().lastIndexOf(".");
        if (lastIndexOf != -1) {
            name += activityImage.getOriginalFilename().substring(lastIndexOf);
        }
        //文件上传到MINIO服务器
        minioClient.putObject(PutObjectArgs.builder()
                .bucket("topsource-marketactivity")
                .object(name)
                .stream(activityImage.getInputStream(), activityImage.getSize(), -1)
                .build());
        //获得图片URL地址
        String  goodsImageUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket("topsource-marketactivity")
                .object(name)
                .method(Method.GET)
                .build());

        marketActivity.setId(String.valueOf(idWorker.nextId()));
        marketActivity.setCreateDate(WorldTime.chinese_time(new Date()));
        //marketActivity.setBeginDate(WorldTime.chinese_time(marketActivity.getBeginDate()));
        //marketActivity.setEndDate(WorldTime.chinese_time(marketActivity.getEndDate()));
        marketActivity.setActivityImage(goodsImageUrl);
        marketActivityMapper.insertSelective(marketActivity);
        //增加活动需要的商品
        for(ActivityGoods activityGoods : activityGoodsList){
            activityGoods.setId(String.valueOf(idWorker.nextId()));
            activityGoods.setMarketId(marketActivity.getId());
            Goods goods = (Goods)redisTemplate.boundHashOps(SystemConstants.redis_goods).get(activityGoods.getGoodsId());
            activityGoods.setGoodsName(goods.getName());
            activityGoods.setBrandName(goods.getBrandName());
            activityGoods.setGoodsImage(goods.getImage());
            activityGoods.setCreated(WorldTime.chinese_time(new Date()));
            activityGoodsMapper.insertSelective(activityGoods);
        }
        return marketActivity.getId();
    }

    @Override
    public Map<String, Object> searchMarketActivity(Map searchParam) throws Exception {
        List<MarketActivity> marketActivityList = null;
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).keys();
        if(ObjectUtils.isEmpty(searchParam)){
            marketActivityList = new ArrayList<MarketActivity>();
            for(Object key : keys){
                MarketActivity marketActivity = (MarketActivity)redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).get(key);
                List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(key);
                marketActivity.setActivityGoodsList(activityGoodsList);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.PATTERN_YYYY_MM_DDHHMMSS);
                marketActivity.setCreateDate(DateUtil.formatStr(simpleDateFormat.format(marketActivity.getCreateDate()),DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                marketActivityList.add(marketActivity);
            }
        }else{
            marketActivityList = new ArrayList<MarketActivity>();
            for(Object key : keys){
                MarketActivity marketActivity = (MarketActivity)redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).get(key);
                Boolean example = createExample(searchParam, marketActivity);
                if (example) {
                    List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(key);
                    marketActivity.setActivityGoodsList(activityGoodsList);
                    marketActivityList.add(marketActivity);
                }
            }

        }
        Map<String, Object> resultMap = pageHelpInfo(searchParam, marketActivityList);
        return resultMap;
    }

    @Override
    public Result reduceActivityGoods(ActivityGoods activityGoods) throws Exception {
        //int i = activityGoodsMapper.selectGoodsNum(activityGoods.getMarketId(), activityGoods.getGoodsId());
        Object goodNum = redisTemplate.boundListOps(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId()).rightPop();
        if(ObjectUtils.isEmpty(goodNum)){
            //activityGoodsMapper.reduceGoodsNum(activityGoods.getId());
            return new Result<>(false,StatusCode.SC_NOT_FOUND,"营销活动商品：" + activityGoods.getGoodsId() + "已售罄！");
        }
        return new Result<>(true,StatusCode.SC_OK,"营销活动商品：" + activityGoods.getGoodsId() + "减少库存成功！");
    }

    @Override
    public Result addActivityGoods(ActivityGoods activityGoods) throws Exception {
        try{
            redisTemplate.boundListOps(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId()).leftPushAll(1);
        } catch (Exception e) {
            throw new RuntimeException("Redis operation(add) redis_activityGoodsNum failed: " + e.getMessage());
        }
        return new Result<>(true,StatusCode.SC_OK,"营销活动商品：" + activityGoods.getGoodsId() + "增加库存成功！");
    }

    @Override
    public void addActivityStatus(Order order) throws Exception {
        ActivityStatus activityStatus = new ActivityStatus();
        activityStatus.setId(String.valueOf(idWorker.nextId()));
        activityStatus.setCreateTime(WorldTime.chinese_time(new Date()));
        activityStatus.setOrderId(order.getId());
        activityStatus.setDescription(order.getDesc());
        activityStatus.setUserId(order.getUserId());
        activityStatus.setGrabStatus(order.getGrabStatus());
        activityStatusMapper.insertSelective(activityStatus);
    }

    @Override
    public List<ActivityStatus> searchActivityStatus(String userId) throws Exception {
        List<ActivityStatus> activityStatusList = (List<ActivityStatus>)redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).get(userId);
        if(ObjectUtils.isEmpty(activityStatusList)){
            activityStatusList = new ArrayList<>();
        }
        return activityStatusList;
    }
    private Map<String, Object> pageHelpInfo(Map searchParam,List<MarketActivity> marketActivityList){
        Map<String, Object> resultMap = null;
        //判断传过来的参数是否有分页信息
        if(searchParam == null || searchParam.isEmpty()){
            resultMap = PageHelp.PageList(marketActivityList);
        }else {
            if(!StringUtils.isEmpty(searchParam.get("currentPage") == null ? "" : searchParam.get("currentPage").toString())
                    && !StringUtils.isEmpty(searchParam.get("pageSize") == null ? "": searchParam.get("pageSize").toString())){
                //如果有进行强转
                int currentPage = (Integer) searchParam.get("currentPage");
                int pageSize = (Integer) searchParam.get("pageSize");
                //检查CurrentPage，pageSize是否小于、等于0，如果有，那么初始一个默认值
                Map<String, Integer> pageInfo = PageHelp.decidePage(currentPage, pageSize);
                currentPage = pageInfo.get("page");
                pageSize = pageInfo.get("pageSize");
                //用PageHelp把userList进行分页操作
                List<MarketActivity> resultList = PageHelp.paginate(marketActivityList, currentPage, pageSize);
                //用PageHelp封装分页信息
                resultMap = PageHelp.PageList(resultList, currentPage, pageSize, marketActivityList.size());
            }else {
                resultMap = PageHelp.PageList(marketActivityList);
            }
        }
        return resultMap;
    }

    private Boolean createExample(Map<String,Object> searchParam, MarketActivity marketActivity){
        Boolean Example = true;
        Object activityName = searchParam.get("activityName");
        if(!StringUtils.isEmpty(activityName == null ? "" : activityName.toString())){
            if (marketActivity.getActivityName().contains(activityName.toString())){
                Example = true;
            }else{
                Example = false;
                return Example;
            }
        }

        Object activityStatus = searchParam.get("activityStatus");
        if(!StringUtils.isEmpty(activityStatus == null ? "" : activityStatus.toString())){
            if(marketActivity.getActivityStatus().equals(activityStatus)){
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
