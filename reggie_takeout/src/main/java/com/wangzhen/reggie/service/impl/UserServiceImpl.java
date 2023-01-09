package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.mapper.UserMapper;
import com.wangzhen.reggie.pojo.User;
import com.wangzhen.reggie.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author wz
 * @ClassName UserServiceImpl
 * @date 2023/1/7 17:19
 * @Description TODO
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
