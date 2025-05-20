package com.zkcompany.service.impl;

import com.zkcompany.dao.GoodsMapper;
import com.zkcompany.entity.IdWorker;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.entity.WorldTime;
import com.zkcompany.fegin.SearchCenterFegin;
import com.zkcompany.pojo.Goods;
import com.zkcompany.pojo.Order;
import com.zkcompany.service.GoodsService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService, UserDetailsService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SearchCenterFegin searchCenterFegin;
    @Override
    public int addGoods(Goods goods, MultipartFile file) throws Exception {
        //给文件重新命名
        String name = "good_image_" + System.currentTimeMillis();

        int lastIndexOf = file.getOriginalFilename().lastIndexOf(".");
        if (lastIndexOf != -1) {
            name += file.getOriginalFilename().substring(lastIndexOf);
        }

        //文件上传到MINIO服务器
        minioClient.putObject(PutObjectArgs.builder()
                .bucket("topsource-goodslist")
                .object(name)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
        //获得图片URL地址
        String  goodsImageUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket("topsource-goodslist")
                .object(name)
                .method(Method.GET)
                .build());

        goods.setId(String.valueOf(idWorker.nextId()));
        goods.setImage(goodsImageUrl);
        goods.setCreateTime(WorldTime.chinese_time(new Date()));
        goods.setUpdateTime(WorldTime.chinese_time(new Date()));

        int i = goodsMapper.insertSelective(goods);
        return i;
    }

    @Override
    public Result searchGoods(Map paramBody) throws Exception {
        Result result = searchCenterFegin.searchKeywords(paramBody);
        return result;
    }

    @Override
    public int updateGoods(Goods good) throws Exception {
        goodsMapper.updateByPrimaryKey(good);
        return 0;
    }

    @Override
    public Result updateGoodsNumInventory(Goods goods, String flag) throws Exception {
        switch (flag){
            case "add":
                goodsMapper.addGoodsNumInventory(goods.getNum(),flag);
                break;
            case "reduce":
                goodsMapper.reduceGoodsNumInventory(goods.getNum(),flag);
                break;
        }

        return new Result(true, StatusCode.SC_OK,"修改商品库存成功！");
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
