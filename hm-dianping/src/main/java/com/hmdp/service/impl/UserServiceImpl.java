package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号是否符合规范
        if (!RegexUtils.isCodeInvalid(phone)) {
            return Result.fail("手机号不合法！");
        }
        // 2.如果符合规范发送验证码
        String code = RandomUtil.randomNumbers(6);

        // 3.将验证码保存到session
        session.setAttribute("code",code);

        // 4.调用阿里云 发送信息
        log.info(code);

        // 5.返回结果
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        /*
            这段代码是由问题的：手机和格式正确，验证码存在即可登录。
            也就意味着并不需要手机号和验证码统一，随便用户都可以登录。
         */
        // 1.获取前端提交验证码和 缓存验证码 以及手机号
        String code = loginForm.getCode();
        Object cacheCode = session.getAttribute("code");
        String phone = loginForm.getPhone();
        // 2.校验手机号验证码是否相符合
        if (!RegexUtils.isCodeInvalid(phone)) {
            return Result.fail("手机号不合法！");
        }
        if (code == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误！");
        }
        // 3.根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 4.判断用户是否存在
        if (user == null) {
            user = createUser(phone);
        }
        // 5.保存至session
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        // 6.返回结果
        return Result.ok();
    }

    private User createUser(String phone) {
        User user = new User();
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(7));
        user.setPhone(phone);
        save(user);
        return user;
    }
}
