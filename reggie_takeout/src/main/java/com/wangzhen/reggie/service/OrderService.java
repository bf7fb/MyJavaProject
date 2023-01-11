package com.wangzhen.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzhen.reggie.pojo.OrderDetail;
import com.wangzhen.reggie.pojo.Orders;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    List<Orders> selectOrders(int page, int pageSize, String number, String beginTime, String endTime);

    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId);


    public Page selectOrdersPage(int page, int pageSize);

    public void again(Map<String,String> map);
}
