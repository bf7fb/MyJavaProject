package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.mapper.ShoppingCartMapper;
import com.wangzhen.reggie.pojo.ShoppingCart;
import com.wangzhen.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wz
 * @ClassName ShoppingCartServiceImpl
 * @date 2023/1/8 15:50
 * @Description TODO
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {
}
