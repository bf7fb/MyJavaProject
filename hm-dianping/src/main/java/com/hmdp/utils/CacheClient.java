package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * @author wz
 * @ClassName CacheClient
 * @date 2023/3/22 9:24
 * @Description TODO
 */
@Component
@Slf4j
public class CacheClient {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存穿透：设置key值
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */

    public void set(String key, Object value, Long time, TimeUnit timeUnit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,timeUnit);
    }

    /**
     * 设置空值解决缓存穿透
     * @param Idprefix key的Id前缀
     * @param id 对象Id
     * @param type 对象类型
     * @param dbFallback 查询函数
     * @param keyTime key过期时间
     * @param time
     * @param cacheNullTime 缓存的空值有效时间
     * @param timeUnit
     * @return
     * @param <R>
     * @param <ID>
     */
   public <R,ID> R queryWithPassThrough(
           String Idprefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long keyTime, TimeUnit time,
           Long cacheNullTime, TimeUnit timeUnit){
       String key = Idprefix + id;
       String json = stringRedisTemplate.opsForValue().get(key);
       // 2.判断商铺信息是否存在，存在返回商铺信息 isNotBlank null "" 都返回false
       if (StringUtils.isNotBlank(json)) {
           R r = JSONUtil.toBean(json, type);
           return r;
       }
       // 对缓存中数据进行判断 看是否为null或""  不为空肯定就是""空字符串了
//        if ("".equals(shopJson)) {
       if (json != null) {
           // 返回错误信息(null 代表错误信息)
           return null;
       }
       // 3.不存在，查询数据库
       R r = dbFallback.apply(id);
       // 4.不存在，返回
       if (r == null) {
           // 如果数据库中数据不存在 则在缓存中设置空值 防止缓存穿透
           stringRedisTemplate.opsForValue().set(key,"",cacheNullTime, timeUnit);

           return null;
       }
       // 5.存在，将商品信息存放到redis
       this.set(key,r,keyTime,time);

       return r;
   }

    /**
     * 利用redis中setnx命令加锁解决缓存击穿
     * @param Idprefix key的Id前缀
     * @param id 对象Id
     * @param type 对象类型
     * @param dbFallback 查询函数
     * @param keyTime key过期时间
     * @param time
     * @param cacheNullTime 缓存的空值有效时间
     * @param timeUnit
     * @return
     * @param <R>
     * @param <ID>
     */
   public <R,ID> R queryWithMutex(String Idprefix,ID id,Class<R> type,Function<ID,R> dbFallback,
                                  Long keyTime, TimeUnit time,
                                  Long cacheNullTime, TimeUnit timeUnit){
       // 1.获取key
       String key = Idprefix + id;
       String json = stringRedisTemplate.opsForValue().get(key);
       // 2.判断商铺信息是否存在，存在返回商铺信息 isNotBlank null "" 都返回false
       if (StringUtils.isNotBlank(json)) {
           R r = JSONUtil.toBean(json, type);
           return r;
       }
       // 对缓存中数据进行判断 看是否为null或""  不为空肯定就是""空字符串了
//        if ("".equals(shopJson)) {
       if (json != null) {
           // 返回错误信息(null 代表错误信息)
           return null;
       }
       // 3.尝试获取互斥锁
       String lockKey = LOCK_SHOP_KEY + id;
       boolean flag = tryToGetLock(lockKey);
       // 3.1 判断是否获取到互斥锁
       // 3.2 没有获取到 休眠
       R r = null;
       try {
           if (!flag) {
               Thread.sleep(20);
               // 3.3 休眠后再查redis(递归)
               return queryWithMutex(Idprefix,id,type,dbFallback,keyTime,time,cacheNullTime,timeUnit);
           }

           // 4 获取到查询数据库
             r = dbFallback.apply(id);
           // 5.不存在，返回
           if (r == null) {
               // 如果数据库中数据不存在 则在缓存中设置空值 防止缓存穿透
               stringRedisTemplate.opsForValue().set(key,"",cacheNullTime, timeUnit);

               return null;
           }
           // 6.存在，将商品信息存放到redis
           this.set(key,r,keyTime,time);

           // 7.释放互斥锁
       } catch (InterruptedException e) {
           throw new RuntimeException(e);
       } finally {
           unlock(lockKey);

       }

       return r;
   }

    private boolean tryToGetLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);

    }

    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }


}
