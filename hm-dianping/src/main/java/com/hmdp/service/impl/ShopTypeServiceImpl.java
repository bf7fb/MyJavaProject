package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wz
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public List<ShopType> queryShopTypeList() {
        // 1.从缓存中查询店铺类型信息
        String shopTypeKey = CACHE_SHOPTYPE_KEY;
        String shoptypeJson = stringRedisTemplate.opsForValue().get(shopTypeKey);
        // 2.判断是否存在 存在则返回
        if (StringUtils.isNotBlank(shoptypeJson)) {
            List<ShopType> shopTypes = JSONUtil.toList(shoptypeJson, ShopType.class);
            return shopTypes;
        }
        // 3.不存在，则查询数据库
        List<ShopType> shoptypeList = query().orderByAsc("sort").list();
        // 4.数据库不存在则返回空集合
        if (shoptypeList == null) {
            return new ArrayList<ShopType>();
        }
        // 4.查询数据库存在 保存至redis
        stringRedisTemplate.opsForValue().set(shopTypeKey,JSONUtil.toJsonStr(shoptypeList),CACHE_SHOPTYPE_TTL, TimeUnit.MINUTES);
        // 5.返回查询结果
        return shoptypeList;
    }
}
