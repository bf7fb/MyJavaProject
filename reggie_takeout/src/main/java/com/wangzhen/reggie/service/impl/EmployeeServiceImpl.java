package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.mapper.EmployeeMapper;
import com.wangzhen.reggie.pojo.Employee;
import com.wangzhen.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author wz
 * @ClassName EmployeeService
 * @date 2023/1/5 16:22
 * @Description TODO
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
