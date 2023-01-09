package com.wangzhen.reggie.dto;

import com.wangzhen.reggie.pojo.OrderDetail;
import com.wangzhen.reggie.pojo.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
