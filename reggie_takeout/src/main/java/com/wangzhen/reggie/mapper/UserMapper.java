package com.wangzhen.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzhen.reggie.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wz
 * @ClassName UserMapper
 * @date 2023/1/7 17:18
 * @Description TODO
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
