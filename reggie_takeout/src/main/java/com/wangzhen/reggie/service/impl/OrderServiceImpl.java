package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.common.BaseContext;
import com.wangzhen.reggie.common.CustomException;
import com.wangzhen.reggie.dto.OrdersDto;
import com.wangzhen.reggie.mapper.OrderMapper;
import com.wangzhen.reggie.pojo.*;
import com.wangzhen.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        // 保证线程安全 金额不会出错
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        // 这里不能通过user(登录账号)去获取订单人姓名 应该通过地址簿信息去获取姓名
//        orders.setUserName(user.getName());
        orders.setUserName(addressBook.getConsignee());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }

    /**
     * 后端查询订单
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public List<Orders> selectOrders(int page, int pageSize, String number, String beginTime, String endTime) {
        return orderMapper.selectOrders(page, pageSize, number, beginTime, endTime);
    }

    /**
     * 前端根据orderId查询orderDetail
     * select * from order_detail where order_id = xxx; 一个订单(一个orderId) 对应多条购买记录
     * @param orderId
     * @return
     */
    @Override
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }

    /**
     * 前端分页查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page selectOrdersPage(int page, int pageSize) {
        //1.分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> pageDto = new Page<>(page,pageSize);
        //2.构造条件查询对象 传入userId
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        //2.1添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //3.分页查询orders表
        this.page(pageInfo,queryWrapper);

        //4.通过OrderId查询对应的OrderDetail
        LambdaQueryWrapper<OrderDetail> queryWrapper2 = new LambdaQueryWrapper<>();

        //4.1对OrderDto进行需要的属性赋值 records是orders表中的每条记录
        List<Orders> records = pageInfo.getRecords();
        //4.2遍历orders表 获取orderId 调用getOrderDetailListByOrderId方法完成对orderDetail表的查询
        // 即一个订单的查询     遍历结束 查询全部订单中的全部数据结束
        // orderDtoList是一个集合存放所有订单，每一个订单的具体数据又是一个orderDetails集合
        List<OrdersDto> orderDtoList = records.stream().map((item) ->{
            OrdersDto orderDto = new OrdersDto();
            //4.3此时的orderDto对象里面orderDetails属性还是空 下面准备为它赋值 即将item拷贝之orderDto
            Long orderId = item.getId();//获取订单id
            List<OrderDetail> orderDetailList = this.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item,orderDto);
            //4.4 对orderDto进行OrderDetails属性的赋值 即一个订单中的具体菜品数据赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());

        //5.将订单信息orders拷贝之orderDto 再填充订单具体数据orderDtoList
        System.out.println("pageInfo===="+pageInfo.getRecords());
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        System.out.println("pageDto1====" + pageDto.getRecords());
        pageDto.setRecords(orderDtoList);
        System.out.println("pageDto2====" + pageDto.getRecords());
        return pageDto;

    }

    /**
     * 操作shoppingCart表
     * 注意再来一单 只是将数据回显至购物车中 不下单 下单具体操作仍由submit方法执行
     * @param map
     */
    @Override
//    @Transactional
    public void again(Map<String, String> map) {
        // 1.首先根据user_id清除购物车 避免再来一单数据和原购物车数据混合
        // 1.1获取userId
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<ShoppingCart> qw = new QueryWrapper<>();
        qw.eq("user_id",userId);
        shoppingCartService.remove(qw);

        // 2.根据order_id查询该订单数据 填充至购物车
        QueryWrapper<OrderDetail> qw1 = new QueryWrapper<>();
        qw1.eq("order_id",map.get("id"));
        List<OrderDetail> list = orderDetailService.list(qw1);
        // 3.遍历list，初始化shoppingCart
//        ShoppingCart shoppingCart = new ShoppingCart();
//        for (OrderDetail orderDetail : list) {
//            shoppingCart.setName(orderDetail.getName());
//            shoppingCart.setImage(orderDetail.getImage());
//            shoppingCart.setUserId(BaseContext.getCurrentId());
//            shoppingCart.setDishId(orderDetail.getDishId());
//            shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
//            shoppingCart.setNumber(orderDetail.getNumber());
//            orderDetail.setAmount(orderDetail.getAmount());
//        }
        List<ShoppingCart> shoppingCartList = list.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        // 4.保存至shoppingCart表
        shoppingCartService.saveBatch(shoppingCartList);
    }
}