package com.zykj.purchase.common;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 如果key存在的话返回fasle 不存在的话返回true
    public Boolean setNx(String key, String value, Long timeout) {
        Boolean setIfAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
        if (timeout != null) {
            stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        }
        return setIfAbsent;
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }

    public void setList(String key, List<String> listToken) {
        stringRedisTemplate.opsForList().leftPushAll(key, listToken);
    }

    public void pushList(String key, String listToken) {
        stringRedisTemplate.opsForList().leftPushAll(key, listToken);
    }

    public List<String> getRandomValue(String key, int size) {
        return stringRedisTemplate.opsForSet().randomMembers(key, size);
    }

    public Set<String> getSet(String prefix, String key) {
        Set<String> members = stringRedisTemplate.opsForSet().members(prefix + key);
        return members;
    }

    public Long setSet(String prefix, String key, String value) {
        return stringRedisTemplate.opsForSet().add(prefix + key, value);
    }

    public boolean examineSet(String prefix, String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(prefix + key, value);
    }

    public Integer getSetSize(String prefix, String key) {
        Set<String> set = getSet(prefix, key);

        return set.size();
    }

    public Integer getSetSizeTwo(String prefix, String key) {

        Long size = stringRedisTemplate.opsForSet().size(prefix + key);

        return Integer.valueOf(size.toString());
    }

    public String getZSetPageSort(String prefix, String key, long currentPage, long pageNum) {

        Set<String> reverseRangeWithScores = stringRedisTemplate.opsForZSet().reverseRangeByScore(prefix + key, 0,
                System.currentTimeMillis(), (currentPage - 1) * pageNum, pageNum);

        return JSONObject.toJSONString(reverseRangeWithScores);
    }

//	public void addZSet(String prefix, String key, String v, long t) {
//		stringRedisTemplate.opsForZSet().add(prefix + key, v, t);
//	}

    public void incr(String key) {
        stringRedisTemplate.opsForValue().increment(key, 1);
    }

    public void decrease(String key) {
        stringRedisTemplate.opsForValue().increment(key, -1);
    }

    public void removeUnread(String key) {
        setString(key, "0");
    }

    public void removeSet(String prefix, String key, String value) {
        stringRedisTemplate.opsForSet().remove(prefix + key, value);
    }

    public void incrementScore(String prefix, String key, double value) {
        Double score = stringRedisTemplate.opsForZSet().incrementScore(prefix, key, value);

    }

    public String getTop(String key) {

        Set<ZSetOperations.TypedTuple<String>> rangeWithScores = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, 10);
        return JSONObject.toJSONString(rangeWithScores);

    }


    public Long getListLength(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    /**
     * 存放string类型
     *
     * @param key     key
     * @param data    数据
     * @param timeout 超时间
     */
    public void setString(String key, String data, Long timeout) {
        try {

            stringRedisTemplate.opsForValue().set(key, data);
            if (timeout != null) {
                stringRedisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {

        }

    }

    public void setStringSECONDS(String key, String data, Long timeout) {
        try {

            stringRedisTemplate.opsForValue().set(key, data);
            if (timeout != null) {
                stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            }

        } catch (Exception e) {

        }

    }

    /**
     * 开启Redis 事务
     *
     * @param
     */
    public void begin() {
        // 开启Redis 事务权限
        stringRedisTemplate.setEnableTransactionSupport(true);
        // 开启事务
        stringRedisTemplate.multi();

    }

    /**
     * 提交事务
     *
     * @param
     */
    public void exec() {
        // 成功提交事务
        stringRedisTemplate.exec();
    }

    /**
     * 回滚Redis 事务
     */
    public void discard() {
        stringRedisTemplate.discard();
    }

    /**
     * 存放string类型
     *
     * @param key  key
     * @param data 数据
     */
    public void setString(String key, String data) {
        setString(key, data, null);
    }

    /**
     * 根据key查询string类型
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value;
    }

    /**
     * 根据对应的key删除key
     *
     * @param key
     */
    public Boolean delKey(String key) {
        return stringRedisTemplate.delete(key);

    }

    public void addZset(String key, String value, long score) {
        stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    public Double incrZsetScore(String key, String value, long score) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    public Double scoreZet(String key, String value) {
        return stringRedisTemplate.opsForZSet().score(key, value);
    }

    public Long rankZet(String key, String value) {

        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    public Long getZsetSize(String key) {

        return stringRedisTemplate.opsForZSet().zCard(key);

    }

    // 锁的键值
    private String lockKey;
    // 锁超时, 防止线程得到锁之后, 不去释放锁
    private int expireMsecs = 15 * 1000;
    // 锁等待, 防止线程饥饿
    private int timeoutMsecs = 15 * 1000;
    // 是否已经获取锁
    private boolean locked = false;

    public RedisUtil setLock(String lockKey, int expireMsecs, int timeoutMsecs) {
        this.lockKey = lockKey;
        this.expireMsecs = expireMsecs;
        return this;
    }
    public RedisUtil setLock(String lockKey) {
        this.lockKey = lockKey;
        return this;
    }

    /**
     * 获取锁
     * @return
     */
    public synchronized boolean acquire() {

        int timeout = timeoutMsecs;

        try {

            while (timeout >= 0) {

                long expires = System.currentTimeMillis() + expireMsecs + 1;
                // 锁到期时间
                String expiresStr = String.valueOf(expires);

                if (stringRedisTemplate.opsForValue().setIfAbsent(lockKey, expiresStr)) {
                    locked = true;
                    log.info("[1] 成功获取分布式锁!");
                    return true;
                }
                // redis里的时间
                String currentValueStr = stringRedisTemplate.opsForValue().get(lockKey);

                // 判断是否为空, 不为空的情况下, 如果被其他线程设置了值, 则第二个条件判断是过不去的
                if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {

                    String oldValueStr = stringRedisTemplate.opsForValue().getAndSet(lockKey, expiresStr);

                    // 获取上一个锁到期时间, 并设置现在的锁到期时间
                    // 只有一个线程才能获取上一个线程的设置时间
                    // 如果这个时候, 多个线程恰好都到了这里, 但是只有一个线程的设置值和当前值相同, 它才有权利获取锁
                    if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                        locked = true;
                        log.info("[2] 成功获取分布式锁!");
                        return true;
                    }
                }

                timeout -= 100;
                Thread.sleep(100);
            }
        } catch (Exception e) {
            log.error("获取锁出现异常, 必须释放: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 释放锁
     */
    public synchronized void release() {

        try {
            if (locked) {
                // redis里的时间
                String currentValueStr = stringRedisTemplate.opsForValue().get(lockKey);

                // 校验是否超过有效期, 如果不在有效期内, 那说明当前锁已经失效, 不能进行删除锁操作
                if (currentValueStr != null && Long.parseLong(currentValueStr) > System.currentTimeMillis()) {
                    stringRedisTemplate.delete(lockKey);
                    locked = false;
                    log.info("[3] 成功释放分布式锁!");
                }
            }
        } catch (Exception e) {
            log.error("释放锁出现异常, 必须释放: {}", e.getMessage());
        }
    }
}
