package com.wangzhen.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzhen.reggie.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wz
 * @ClassName EmployeeMapper
 * @date 2023/1/5 16:49
 * @Description TODO
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
