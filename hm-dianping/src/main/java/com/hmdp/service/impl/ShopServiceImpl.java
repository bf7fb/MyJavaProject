package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wz
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    /**
     * 最初的方法 解决缓存雪崩 直接返回result结果
     * @param id
     * @return
     */
    /*
    @Override
    public Result queryById(Long id) {
        // 1.从redis查询商铺信息
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断商铺信息是否存在，存在返回商铺信息 isNotBlank null "" 都返回false
        if (StringUtils.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 对缓存中数据进行判断 看是否为null或""  不为空肯定就是""空字符串了
//        if ("".equals(shopJson)) {
            if (shopJson != null) {
            return Result.fail("店铺信息不存在");
        }
        // 3.不存在，查询数据库
        Shop shop = getById(id);
        // 4.不存在，返回
        if (shop == null) {
            // 如果数据库中数据不存在 则在缓存中设置空值 防止缓存穿透
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);

            return Result.fail("商铺不存在！");
        }
        // 5.存在，将商品信息存放到redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 6.返回商铺信息
        return Result.ok(shop);
    }
     */

    @Override
    public Result queryById(Long id) {
        // 1.解决缓存穿透
//        Shop shop = queryWithPassThrough(id);
//        Shop shop = cacheClient.queryWithPassThrough(
//                CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES,
//                CACHE_NULL_TTL, TimeUnit.SECONDS);

        // 2.解决缓存击穿
//        Shop shop = queryWithMutex(id);
        Shop shop = cacheClient.queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById,
                CACHE_SHOP_TTL, TimeUnit.MINUTES,
                CACHE_NULL_TTL, TimeUnit.SECONDS);
        if (shop == null) {
            return Result.fail("查询店铺错误！");
        }
        return Result.ok(shop);
    }

    /**
     * 缓存击穿 利用互斥锁解决 原理：redis中setnx命令只有当key为空时才允许赋值
     * @param id
     * @return
     */
    private Shop queryWithMutex(Long id){
        // 1.获取key
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断商铺信息是否存在，存在返回商铺信息 isNotBlank null "" 都返回false
        if (StringUtils.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 对缓存中数据进行判断 看是否为null或""  不为空肯定就是""空字符串了
//        if ("".equals(shopJson)) {
        if (shopJson != null) {
            // 返回错误信息(null 代表错误信息)
            return null;
        }
        // 3.尝试获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean flag = tryToGetLock(lockKey);
        // 3.1 判断是否获取到互斥锁
        // 3.2 没有获取到 休眠
        Shop shop = null;
        try {
            if (!flag) {
                Thread.sleep(20);
                // 3.3 休眠后再查redis(递归)
                return queryWithMutex(id);
            }

            // 4 获取到查询数据库
            shop = getById(id);
            // 5.不存在，返回
            if (shop == null) {
                // 如果数据库中数据不存在 则在缓存中设置空值 防止缓存穿透
                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);

                return null;
            }
            // 6.存在，将商品信息存放到redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);

            // 7.释放互斥锁
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unlock(lockKey);

        }

        return shop;
    }

    /**
     * 缓存穿透解决方案：设置空值
     * @param id
     * @return
     */
    private Shop queryWithPassThrough(Long id){
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断商铺信息是否存在，存在返回商铺信息 isNotBlank null "" 都返回false
        if (StringUtils.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 对缓存中数据进行判断 看是否为null或""  不为空肯定就是""空字符串了
//        if ("".equals(shopJson)) {
        if (shopJson != null) {
            // 返回错误信息(null 代表错误信息)
            return null;
        }
        // 3.不存在，查询数据库
        Shop shop = getById(id);
        // 4.不存在，返回
        if (shop == null) {
            // 如果数据库中数据不存在 则在缓存中设置空值 防止缓存穿透
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);

            return null;
        }
        // 5.存在，将商品信息存放到redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);

        return shop;
    }

    /**
     * redis中setnx命令 只有key为空时才可以设置值
     * @param key
     * @return
     */
    private boolean tryToGetLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);

    }

    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }



    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        // 1.判断是否获取到shop
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空~");
        }
        // 2.获取到 进行数据库修改
        updateById(shop);
        // 3.更新缓存
        String shopKey = CACHE_SHOP_KEY + id;
        stringRedisTemplate.delete(shopKey);
        return Result.ok();
    }
}
