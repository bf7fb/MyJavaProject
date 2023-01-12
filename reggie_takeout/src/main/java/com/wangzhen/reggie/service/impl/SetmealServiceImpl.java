package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.common.CustomException;
import com.wangzhen.reggie.dto.SetmealDto;
import com.wangzhen.reggie.mapper.SetmealMapper;
import com.wangzhen.reggie.pojo.Setmeal;
import com.wangzhen.reggie.pojo.SetmealDish;
import com.wangzhen.reggie.service.CategoryService;
import com.wangzhen.reggie.service.SetmealDishService;
import com.wangzhen.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wz
 * @ClassName SetmealServiceImpl
 * @date 2023/1/6 17:03
 * @Description TODO
 */
@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void addSetmeal(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }


    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    public void removeSetmealWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 修改起售停售套餐
     * @param statusCode
     * @param ids
     */
    @Override
    public void updateStatus(Integer statusCode, List<Long> ids) {
        setmealMapper.updateStatus(statusCode, ids);
    }

    /**
     * 修改套餐前回显套餐数据
     * @param id
     * @return
     */
    @Override
    public SetmealDto reviewSetmealWhenUpdateSetmeal(Long id) {
        //1.查询套餐
        Setmeal setmeal = this.getById(id);
        //2.初始化dto
        SetmealDto setmealDto = new SetmealDto();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        //在关联表中查询，setmealdish select * from setmeal_dish where setmeal_id = id;
        queryWrapper.eq(id!=null,SetmealDish::getSetmealId,id);

        if (setmeal != null){
            // 将setmeal拷贝纸setmealdato setmealDishes属性
            BeanUtils.copyProperties(setmeal,setmealDto);
            List<SetmealDish> list = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }
        return null;

    }

    /**
     * 修改套餐
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateSetmeal(SetmealDto setmealDto) {
        //1.为空则返回
        if (setmealDto == null){
            return ;
        }

        //2. 修改操作前，根据setmealId删除setmeal_dish表中中原内容
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(queryWrapper);


        //3.前端setmealDishs中不含setmeal_id  因此我们要手动赋值
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        //4.获取提交setmealDishs数据 批量把setmealDish保存到setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
        //5.修改setmeal表 mp会通过setmealDto获取id 然后根据id自动修改表单数据
        this.updateById(setmealDto);

    }


}
