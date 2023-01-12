package com.wangzhen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzhen.reggie.common.Result;
import com.wangzhen.reggie.dto.DishDto;
import com.wangzhen.reggie.dto.SetmealDto;
import com.wangzhen.reggie.pojo.Category;
import com.wangzhen.reggie.pojo.Dish;
import com.wangzhen.reggie.pojo.Setmeal;
import com.wangzhen.reggie.pojo.SetmealDish;
import com.wangzhen.reggie.service.CategoryService;
import com.wangzhen.reggie.service.DishService;
import com.wangzhen.reggie.service.SetmealDishService;
import com.wangzhen.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wz
 * @ClassName SetmealController
 * @date 2023/1/7 15:59
 * @Description TODO
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     * allEntries = true 删除setmealCache分类缓存下的所有数据
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);

        setmealService.addSetmeal(setmealDto);

        return Result.success("新增套餐成功");
    }

    /**
     * 后端套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return Result.success(dtoPage);
    }

    /**
     * 后端删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true) // 该注解作用删除套餐时 将缓存中的全部数据删除
    public Result<String> deleteSetmealWithDish(@RequestParam List<Long> ids) {
        setmealService.removeSetmealWithDish(ids);
        return Result.success("删除成功~");
    }

    /**
     * 前端查询套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public Result<List<Setmeal>> list(Setmeal setmeal) {
        log.info("setmeal:{}", setmeal);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(setmeal.getName()), Setmeal::getName, setmeal.getName());
        queryWrapper.eq(null != setmeal.getCategoryId(), Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(null != setmeal.getStatus(), Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return Result.success(setmealService.list(queryWrapper));
    }

    /**
     * 停售/起售/批量停起售套餐
     * @param statusCode
     * @param ids
     * @return
     */
    @PostMapping("/status/{statusCode}")
    @CacheEvict(value = "setmealCache",allEntries = true) // 该注解作用删除套餐时 将缓存中的全部数据删除
    public Result<String> updateStatus(@PathVariable Integer statusCode, @RequestParam List<Long> ids){
        setmealService.updateStatus(statusCode, ids);
        return Result.success("修改成功");
    }

    /**
     * 修改套餐时 回显套餐数据
     * @param setMealId
     * @return
     */
    @GetMapping("/{setMealId}")
    public Result<SetmealDto> reviewSetmealWhenUpdateSetmeal(@PathVariable Long setMealId){
        SetmealDto setmealDto = setmealService.reviewSetmealWhenUpdateSetmeal(setMealId);
        return Result.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result<String> updateSetmeal(@RequestBody SetmealDto setmealDto){
        setmealService.updateSetmeal(setmealDto);
        return Result.success("修改成功~");
    }

    /**
     * 点击套餐图片 显示具体套餐
     * select * from setmeal_dish where setmeal_id = xxx;
     * @param SetmealId
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishDto>> dish(@PathVariable("id") Long SetmealId) {
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, SetmealId);
        //获取套餐里面的所有菜品  这个就是SetmealDish表里面的数据
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //其实这个BeanUtils的拷贝是浅拷贝，这里要注意一下
            BeanUtils.copyProperties(setmealDish, dishDto);
            //这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtos);
    }

}
