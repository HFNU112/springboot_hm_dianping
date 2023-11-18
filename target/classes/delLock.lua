local key = KEYS[1]
-- 1.获取锁标识
local id = redis.call('get', key)
-- 2.获取当前线程标识
local threadId = ARGV[1]
-- 3.判断当前线程和锁标识是否一致
if(id == threadId)
then
    -- 一致，删除锁
    return redis.call('del', key)
end
-- 不一致
return 0