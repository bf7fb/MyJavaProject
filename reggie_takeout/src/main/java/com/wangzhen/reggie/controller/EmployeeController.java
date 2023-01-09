package com.wangzhen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzhen.reggie.common.Result;
import com.wangzhen.reggie.pojo.Employee;
import com.wangzhen.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wz
 * @ClassName EmployeeController
 * @date 2023/1/5 16:54
 * @Description TODO
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能
     * @param httpServletRequest
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest httpServletRequest, @RequestBody Employee employee){
        //1.进行md5加密处理
        String password = employee.getPassword();
        password  = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据username查询数据库 eq方法第一个参数是数据库字段名称 第二个是字段的值
        LambdaQueryWrapper<Employee> querryMapper = new LambdaQueryWrapper<Employee>();
        querryMapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(querryMapper);

        //3.如果没有查询到则返回 即数据库不存在该用户
        if (emp == null){
            return Result.error("账号或密码错误,请重试~");
        }

        //4.如果密码错误 则返回
        if (!emp.getPassword().equals(password)){
            return Result.error("账号或密码错误,请重试~");
        }

        //5.如果该账户被禁用 则返回
        if (emp.getStatus() == 0){
            return Result.error("该账户被禁用！");
        }

        //6.登陆成功 将id存放至session
        httpServletRequest.getSession().setAttribute("employee",emp.getId());

        //7.登录成功
        return Result.success(emp);

    }

    /**
     * 退出登录
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest httpServletRequest){
        httpServletRequest.getSession().removeAttribute("employee");
        return Result.success("退出成功~");
    }

    /**
     *  添加员工
     * @param employee
     * @param request
     * @return
     */
    @PostMapping
    public Result<String> addEmployee(@RequestBody Employee employee,HttpServletRequest request){
        //1.设置初始密码123456 并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //2.设置基础信息
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        // 获取登录后session中的登录对象
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        //3.保存用户
        employeeService.save(employee);

        //4.返回
        return Result.success("新增员工成功~");

    }

    /**
     * 分页查询/条件查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page<Employee>> page(int page, int pageSize, String name){
        //1.构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //2.构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //3.添加过滤条件 即条件查询where后是否添加name
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getUsername,name);
        //4.添加排序条件 按添加时间排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //5.执行查询
        employeeService.page(pageInfo,queryWrapper);
        //6.返回查询结果
        return Result.success(pageInfo);

    }

    /**
     * 设置员工启用状态  注意：修改员工表单复用了此方法
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> updateStatus(HttpServletRequest request,@RequestBody Employee employee){
        //1.设置修改用户时间和状态
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        //2.调用service完成修改 mp会自动修改 为null的字段不修改保持原值 status前端做了修改
        employeeService.updateById(employee);
        //3.返回结果
        return Result.success("修改成功~");
    }

    /**
     * 修改员工前回显表单
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> reviewEmployee(@PathVariable Long id){
        Employee emp = employeeService.getById(id);
        if (emp != null){
            return Result.success(emp);
        }
        return Result.error("没有查询到对应员工信息~");
    }
}
