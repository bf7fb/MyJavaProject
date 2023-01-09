package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.common.CustomException;
import com.wangzhen.reggie.mapper.CategoryMapper;
import com.wangzhen.reggie.pojo.Category;
import com.wangzhen.reggie.pojo.Dish;
import com.wangzhen.reggie.pojo.Setmeal;
import com.wangzhen.reggie.service.CategoryService;
import com.wangzhen.reggie.service.DishService;
import com.wangzhen.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wz
 * @ClassName CategoryServiceImpl
 * @date 2023/1/6 16:23
 * @Description TODO
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 删除菜品或套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if(dishCount > 0){
            //已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件 根据id查询
        // select count(*) from setmeal where category_id = xxx;
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int setMealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setMealCount > 0){
            throw new CustomException("当前套餐下关联了菜品，不能删除");
        }

        //正常删除
        super.removeById(id);

    }
}
