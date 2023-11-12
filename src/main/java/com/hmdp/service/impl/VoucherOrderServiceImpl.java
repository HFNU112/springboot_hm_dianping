package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.hmdp.utils.RedisConstants.LOCK_ORDER_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 秒杀下单
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        //1. 根据优惠券id查询优惠券信息
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        //2. 判断优惠券的日期是否生效
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            //如果没有生效
            return Result.fail("尚未到抢购时间，请您稍后在来...");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("本次活动已结束，请您关注后续...");
        }
        //3. 时间生效，判断库存是否充足
        if (voucher.getStock() < 1) {
            return Result.fail("很抱歉，本次活动已经抢完了");
        }
        // 5.一人下一单需求
        Long userId = UserHolder.getUser().getId();
        // 8. 在集群环境下 并发执行
        //创建锁对象
        SimpleRedisLock lock = new SimpleRedisLock(userId.toString(), stringRedisTemplate);
        //获取锁
        boolean isLock = lock.tryLock(1200L);
        if (!isLock) {
            return Result.fail("当前用户不允许重复下单！");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            //释放锁
            lock.delLock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrder(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        //5.1 根据优惠券id和用户id查询订单
        Integer count = query()
                .eq("user_id", userId).eq("voucher_id", voucherId)
                .count();
        //5.2 判断当前用户是否下单
        if (count > 0) {
            // 下单成功，已经下单返回已下单
            return Result.fail("用户不能重复下单");
        }
        // 未下单，扣减库存
        boolean isSuccess = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        // 如果库存不足，返回错误
        if (!isSuccess) {
            return Result.fail("库存不足！");
        }
        //4. 新建一个订单
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId("voucher_order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        //6. 保存订单
        save(voucherOrder);
        // 7. 返回订单id
        return Result.ok(orderId);
    }


}
