package com.wangzhen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzhen.reggie.dto.DishDto;
import com.wangzhen.reggie.pojo.Dish;

/**
 * @author wz
 * @ClassName DishService
 * @date 2023/1/6 17:01
 * @Description TODO
 */
public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    public DishDto selectDishWithFlavor(Long id);
    public void updateDishWithFlavor(DishDto dishDto);


}
