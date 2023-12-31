-- 参数列表
local voucherId = ARGV[1]
local userId = ARGV[2]

-- redis标识 链接两个字符串 ..
local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId
-- 1.判断库存是否充足 GET key
if(tonumber(redis.call('get', stockKey)) <= 0) then
    -- 不充足，返回 1
    return 1
end
-- 2.库存充足，判断用户是否下单 SISMEMBER key member
if(redis.call('SISMEMBER', orderKey, userId) == 1) then
    -- 已下单，返回 2
    return 2
end
-- 2.1 未下单，扣减库存 INCRBY key increment
redis.call('incrby', stockKey, -1)
-- 2.2 当前用户优惠券订单信息存入set集合 SADD key member [member ...]
redis.call('sadd', orderKey, userId)
-- 返回 0
return 0