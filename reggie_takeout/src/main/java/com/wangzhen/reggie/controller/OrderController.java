package com.wangzhen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzhen.reggie.common.Result;
import com.wangzhen.reggie.pojo.Orders;
import com.wangzhen.reggie.service.OrderDetailService;
import com.wangzhen.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return Result.success("下单成功");
    }

    /**
     * 后端查询订单 自造方法
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
//    @GetMapping("/page")
//    public Result<List<Orders>> selectOrders(int page, int pageSize, String number,
//                                        String beginTime,
//                                       String endTime){
//        System.out.println("page pageSize===" + page + "  " + pageSize );
//        System.out.println("number===" + number);
//        System.out.println("localtime===" + beginTime);
//        System.out.println("endTime===" + endTime);
//        List<Orders> orders = orderService.selectOrders(page, pageSize, number, beginTime, endTime);
//        return Result.success(orders);
//    }

    /**
     * 后台查询订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public Result<Page> selectOrders(int page, int pageSize, String number, String beginTime, String endTime){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件  动态sql  字符串使用StringUtils.isNotEmpty这个方法来判断
        //这里使用了范围查询的动态SQL，这里是重点！！！
        // select * from orders where
        // number like '%16%'
        // and order_time between "2023-01-08 12:06:25" and "2023-01-09 17:00:01" limit 1,5;
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime)
                .orderByDesc(true,Orders::getOrderTime);

//        orderService.selectOrders(page, pageSize, number, beginTime, endTime);
        orderService.page(pageInfo,queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 处理订单状态 已派送 已送达 派送中
     * @param map
     * @return
     */
    @PutMapping
    public Result<String> orderStatusChange(@RequestBody Map<String,String> map){

        Long orderId = Long.parseLong(map.get("id"));
        Integer status = Integer.parseInt(map.get("status"));

        if(orderId == null || status==null){
            return Result.error("传入信息不合法");
        }
        //根据订单id 查询订单修改状态
        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);

        return Result.success("订单状态修改成功");

    }

    /**
     * 前端查询历史订单 或最新订单
     * 一个人有多个订单 每一个订单又对应多个orderDetail 是一个多对多
     * 1.select * from orders where user_id = 1611665147010519042 limit 0,3;
     * 2.select * from order_detail where order_id = xxx;
     * 3.合并查询结果
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public Result<Page> selectOrdersPage(int page, int pageSize){
        Page pageInfo = orderService.selectOrdersPage(page, pageSize);
        return Result.success(pageInfo);

    }

    @PostMapping("/again")
//    public Result<OrdersDto> again(@RequestBody Map<String,String> map){
    public Result<String> again(@RequestBody Map<String,String> map){
        System.out.println("id====" + map.get("id"));
        //操作orders 和orderdetail两张表
        orderService.again(map);
        return Result.success("操作成功~");
    }
}