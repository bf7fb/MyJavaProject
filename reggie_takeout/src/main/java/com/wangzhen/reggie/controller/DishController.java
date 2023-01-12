package com.wangzhen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzhen.reggie.common.Result;
import com.wangzhen.reggie.dto.DishDto;
import com.wangzhen.reggie.pojo.Category;
import com.wangzhen.reggie.pojo.Dish;
import com.wangzhen.reggie.pojo.DishFlavor;
import com.wangzhen.reggie.service.CategoryService;
import com.wangzhen.reggie.service.DishFlavorService;
import com.wangzhen.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wz
 * @ClassName DishController
 * @date 2023/1/6 18:52
 * @Description TODO
 */
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> addDish(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return Result.success("添加成功~");
    }

    /**
     * 后端菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝 pageInfo拷贝纸dishDtoPage records属性不拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();


        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    /**
     * 修改菜品前回显数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> reviewDish(@PathVariable Long id){
        DishDto dishDto = dishService.selectDishWithFlavor(id);
        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> updateDish(@RequestBody DishDto dishDto){

        dishService.updateDishWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("新增菜品成功~");
    }

    /**
     * 添加套餐时回显套餐菜品框中菜品数据 种类数据复用了CategoryController中的selectCategoryInAddDish方法
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public Result<List<Dish>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
//        //添加条件，查询状态为1（起售状态）的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return Result.success(list);
//    }

    /**
     * 添加套餐时回显套餐菜品框中菜品数据 菜品种类数据复用了CategoryController中的selectCategoryInAddDish方法
     * 查询菜品时附带查询口味信息 如果有口味前端则显示选择 无口味前端显示+号
     * 前端查询菜品依旧复用此方法
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public Result<List<DishDto>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
//        //添加条件，查询状态为1（起售状态）的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        List<DishDto> dishDtoList = list.stream().map((item) -> {
//            DishDto dishDto = new DishDto();
//
//            BeanUtils.copyProperties(item,dishDto);
//
//            Long categoryId = item.getCategoryId();//分类id
//            //根据id查询分类对象
//            Category category = categoryService.getById(categoryId);
//
//            if(category != null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);
//            }
//
//            //当前菜品的id
//            Long dishId = item.getId();
//            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
//            //SQL:select * from dish_flavor where dish_id = ?
//            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
//            dishDto.setFlavors(dishFlavorList);
//            return dishDto;
//        }).collect(Collectors.toList());
//
//        return Result.success(dishDtoList);
//    }

    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;

        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397844391040167938_1

        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList != null){
            //如果存在，直接返回，无需查询数据库
            return Result.success(dishDtoList);
        }

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return Result.success(dishDtoList);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteDishs(@RequestParam List<Long> ids){
        System.out.println(ids);
        dishService.removeByIds(ids);
        return Result.success("删除成功~");
    }

    /**
     * 修改状态
     * @param statusCode
     * @param ids
     * @return
     */
    @PostMapping("/status/{statusCode}")
    public Result<String> updateStatus(@PathVariable Integer statusCode,@RequestParam List<Long> ids){
//        System.out.println("status===" + statusCode);
//        System.out.println("ids===" + ids);
        // 1.更改数据库状态
        dishService.updateStatus(statusCode, ids);
        // 2.根据ids(dishId) 获取Dish 再根据Dish获取categoryId集合 最后根据categoryId集合 删除缓存数据
        ArrayList<String> KeysCategoryIds = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = dishService.getById(id);
            Long categoryId = dish.getCategoryId();
            KeysCategoryIds.add("dish_" + categoryId + "_1");
        }
        // 3. 删除
        redisTemplate.delete(KeysCategoryIds);
        return Result.success("修改成功");
    }



}
