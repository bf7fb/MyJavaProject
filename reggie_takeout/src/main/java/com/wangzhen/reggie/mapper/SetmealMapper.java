package com.wangzhen.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzhen.reggie.pojo.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wz
 * @ClassName SetmealMapper
 * @date 2023/1/6 17:00
 * @Description TODO
 */
@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {
    void updateStatus(@Param("statusCode") Integer statusCode, @Param("ids") List<Long> ids);
}
