package com.wangzhen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzhen.reggie.dto.SetmealDto;
import com.wangzhen.reggie.pojo.Setmeal;

import java.util.List;

/**
 * @author wz
 * @ClassName SetmealService
 * @date 2023/1/6 17:02
 * @Description TODO
 */
public interface SetmealService extends IService<Setmeal> {
    public void addSetmeal(SetmealDto setmealDto);
    public void removeSetmealWithDish(List<Long> ids);


}
