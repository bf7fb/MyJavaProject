package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.mapper.DishFlavorMapper;
import com.wangzhen.reggie.pojo.DishFlavor;
import com.wangzhen.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * @author wz
 * @ClassName DishFlavorServiceImpl
 * @date 2023/1/6 18:49
 * @Description TODO
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper,DishFlavor> implements DishFlavorService {
}
