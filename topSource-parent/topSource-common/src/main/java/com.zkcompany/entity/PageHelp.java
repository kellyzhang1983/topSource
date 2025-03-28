package com.zkcompany.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageHelp {

    //对List进行分页操作
    public static <T> List<T> paginate(List<T> list, int page, int pageSize) {
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        if (fromIndex > list.size()) {
            return new ArrayList<>();
        }
        return list.subList(fromIndex, toIndex);
    }

    public static Map<String,Integer> decidePage(int page, int pageSize){
        //如果当前页<=0那么自动赋值在第1页
        int decidePage = (page <= 0 ? 1: page);
        //如果当前数据<=0那么自动赋值每页显示10条数据
        int decidePageSize = (pageSize <= 0 ? 10: pageSize);
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        map.put("page",decidePage);
        map.put("pageSize",decidePageSize);
        return map;
    }




    public static <T> Map<String, Object> PageList(List<T> list, int page, int pageSize, int totalNum) {
        HashMap<String, Object> MapList = new HashMap<>();
        MapList.put("page",page);
        MapList.put("pageSize",pageSize);
        MapList.put("totalNum",totalNum);
        int totalPage = 0;
        if(!(totalNum % pageSize == 0)){
            //如果刚刚总数%页数，余数！=0，那么totalNum/pageSize+1=当前页数
            totalPage = totalNum/pageSize + 1;
        }else {
            //如果刚刚总数/页数==0，那么totalNum/pageSize=当前页数
            totalPage = totalNum/pageSize;
        }
        MapList.put("totalPage",totalPage);
        MapList.put("DataList",(List)list);
        return MapList;
    }

    public static <T> Map<String, Object> PageList(List<T> list) {
        HashMap<String, Object> MapList = new HashMap<>();
        MapList.put("totalNum",list.size());
        MapList.put("DataList",(List)list);
        return MapList;
    }
}
