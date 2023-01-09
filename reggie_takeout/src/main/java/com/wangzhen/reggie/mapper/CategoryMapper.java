package com.wangzhen.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzhen.reggie.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wz
 * @ClassName CategoryMapper
 * @date 2023/1/6 16:22
 * @Description TODO
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
