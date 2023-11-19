local key = KEYS[1]
local threadId = ARGV[1]
local releaseTime = ARGV[2]
-- 准备获取锁
-- 1.判断锁是否存在
if(redis.call('exists', key) == 0)''
then
    -- 1.1 不存在，获取锁并添加线程标识
    redis.call('HSET', key, threadId, '1')
    -- 1.2 设置锁的有效期
    redis.call('expire', key, releaseTime)
    return 1
end;
-- 2.判断锁标识是否一致
if(redis.call('HEXISTS', key))
then
    -- 2.1 不一致，获取锁失败锁计数 +1
    redis.call('HINCRBY', key, threadId, '1')
    -- 2.2 设置锁的有效期
    redis.call('expire', key, releaseTime)
    return 1
end;
-- 获取锁失败
return 0
-- 准备释放锁
-- 3. 判断锁是否当前线程一致
if(redis.call('HEXISTS', key, threadId) == 0)
    -- 3.1 锁已释放
    return nil
end
-- 3.2 锁计数 -1
local count = redis.call('HINCRBY', key, threadId, -1)
-- 4.判断计数是否等于0
if(count > 0)
then
    -- 4.2 不等于0 重置锁的有效期
    redis.call('expire', key, releaseTime)
    return nil
else
    -- 4.1 计数等于0 释放锁
    redis.call('HDEL', key, threadId)
    return nil
end