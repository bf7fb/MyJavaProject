package com.wangzhen.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzhen.reggie.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wz
 * @ClassName DishMapper
 * @date 2023/1/6 17:00
 * @Description TODO
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
