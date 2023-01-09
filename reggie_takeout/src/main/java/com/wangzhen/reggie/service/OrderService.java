package com.wangzhen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzhen.reggie.pojo.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);
}
