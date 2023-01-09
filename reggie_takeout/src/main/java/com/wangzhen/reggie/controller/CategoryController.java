package com.wangzhen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzhen.reggie.common.Result;
import com.wangzhen.reggie.pojo.Category;
import com.wangzhen.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wz
 * @ClassName CategoryController
 * @date 2023/1/6 16:24
 * @Description TODO
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 添加菜品或套餐
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> addCategory(@RequestBody Category category){
        categoryService.save(category);
        return Result.success("添加菜品成功~");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize){
        //分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //分页查询
        categoryService.page(pageInfo,queryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 删除套餐或菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> removeDishOrSetMeal(Long ids){
        System.out.println("con id=" + ids);
        categoryService.remove(ids);
        return Result.success("删除成功~");
    }

    /**
     * 修改菜品分类
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> updateCategory(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.success("修改成功~");

    }

    /**
     * category主要是封装了type属性 看是使用菜品分类 还是 套餐
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> selectCategoryInAddDish(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return Result.success(list);
    }

}
