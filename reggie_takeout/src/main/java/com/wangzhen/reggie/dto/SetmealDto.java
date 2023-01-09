package com.wangzhen.reggie.dto;

import com.wangzhen.reggie.pojo.Setmeal;
import com.wangzhen.reggie.pojo.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
