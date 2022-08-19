package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.api.constants.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class RedisService {

    @Resource(name = "redisTemplate")
    RedisTemplate<String, Object> redisTemplate;

    public int findLikeRedis(String key) {
        Set<String> set = redisTemplate.keys(key);
        return set.size();
    }

    public Set<String> getKeys(String pattern){
        return redisTemplate.keys(pattern);
    }

    public void setRedisExpiredTime(String key, Object var, int expiredTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, var, expiredTime, timeUnit);
    }

    public long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public Boolean setRedisExpiredTimeBo(String key, Object var, int expiredTime, TimeUnit timeUnit) {
        if (null != key && !"".equals(key) && null != var && !"".equals(var)) {
            boolean is = redisTemplate.opsForValue().setIfAbsent(key, var);
            if (is) {
                redisTemplate.expire(key, expiredTime, timeUnit);
            }
            return is;
        }
        return Boolean.FALSE;
    }

    public String getKeyAndDel(String key) {
        Object object = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(object)){
            del(key);
            return String.valueOf(object);
        }
        return null;
    }

    public void setRedisValue(String key, Object var) {
        redisTemplate.opsForValue().set(key, var);
    }

    public Boolean booleanRedis(String key) {
        if (Objects.isNull(redisTemplate.opsForValue().get(key))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Object getRedisValus(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public long del(final String... keys) {

        return (Long) redisTemplate.execute((RedisCallback<?>) connection -> {
            long result = 0;
            for (int i = 0; i < keys.length; i++) {
                result = connection.del(keys[i].getBytes());
            }
            return result;
        });
    }

    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute((RedisCallback<?>) connection -> {
            connection.set(key, value);
            if (liveTime > 0) {
                connection.expire(key, liveTime);
            }
            return 1L;
        });
    }

    public void set(String key, String value, long liveTime) {
        this.set(key.getBytes(), value.getBytes(), liveTime);
    }

    public void set(String key, String value) {
        this.set(key, value, 0L);
    }

    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    public String get(final String key) {
        return (String) redisTemplate.execute((RedisCallback<?>) connection -> {
            try {
                return new String(connection.get(key.getBytes()), ApiConstants.REDIS_CODE_KEY);
            } catch (Exception e) {
            }
            return "";
        });
    }

    public boolean exists(final String key) {
        return (Boolean) redisTemplate.execute((RedisCallback<?>) connection -> connection.exists(key.getBytes()));
    }
    
    /**
     * 	将数据放入set缓存
     * 
     * @return 成功数量
     */
    public long addSet(final String key, long time, Object... values) {
    	try {
    		long result = redisTemplate.opsForSet().add(key, values);
    		if (time > 0) {
    			expire(key, time);
    		}
    		return result;
    	} catch (Exception e) {
    		log.error("addSet异常", e);
    		return 0L;
		}
    }
    
    /**
     * 	获取入set里的数量
     */
    public long getSetSize(final String key) {
    	try {
    		return redisTemplate.opsForSet().size(key);
    	} catch (Exception e) {
    		log.error("getSetSize异常", e);
    		return 0L;
    	}
    }
    
    /**
     * 	设置key生存时间
     * 
     * @param key
     * @param time
     * @return
     */
    public boolean expire(String key, long time) {
    	try {
    		if (time > 0) {
    			redisTemplate.expire(key, time,TimeUnit.SECONDS);
    		}
    		return true;
    	} catch (Exception e) {
    		log.error("expire异常", e);
    		return false;
		}
    }
    
}
