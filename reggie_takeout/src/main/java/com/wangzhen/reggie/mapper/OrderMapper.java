package com.wangzhen.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzhen.reggie.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    List<Orders> selectOrders(@Param("page") int page, @Param("pageSize") int pageSize,
                              @Param("number") String number, @Param("beginTime") String beginTime,
                              @Param("endTime") String endTime);

}