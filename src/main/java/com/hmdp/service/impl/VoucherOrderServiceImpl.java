package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hmdp.utils.RedisConstants.LOCK_ORDER_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;



    /**
     * 异步代理订单对象
     */
    IVoucherOrderService proxy;

    /**
     * 异步执行秒杀下单任务的线程池
     */
    public static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 秒杀脚本常量
     */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("secKill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 创建线程任务事件
     */
//    VoucherOrderHandler voucherOrderHandler = new VoucherOrderHandler();

    /**
     * 初始化线程池，提交任务事件到线程池的可用线程
     */
    @PostConstruct
    public void init(){
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }


    /**
     * 当前线程下单提交的任务
     */
    private BlockingQueue<VoucherOrder> ordersTasks = new ArrayBlockingQueue<>(1024 * 1024);

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    //1. 从阻塞队列中取订单信息
                    VoucherOrder voucherOrder = ordersTasks.take();
                    //2. 创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.error("订单处理异常！", e.getMessage());
                }

            }
        }
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder){
        //1.获取用户
        Long userId = voucherOrder.getUserId();

        //2.创建锁对象
        RLock redisLock = redissonClient.getLock(LOCK_ORDER_KEY + userId);

        //3.尝试获取锁
        boolean isLock = redisLock.isLocked();
        //4.判断锁是否获取成功
        if (!isLock) {
            log.error("获取锁失败！");
        }
        try {
            // AOP代理防止spring的事务失效，线程局部变量 ThreadLocale在异步多线程时出现事务失效
            proxy.createVoucherOrderAysnc(voucherOrder);
        } finally {
            // 释放锁
            redisLock.unlock();
        }

    }

    /**
     * 线程池 + 阻塞队列异步优化 - 秒杀下单
     * @param voucherId 优惠券id
     * @return 订单id
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        long orderId = redisIdWorker.nextId("order");
        Long userId = UserHolder.getUser().getId();
        //1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );

        //2.判断结果是否等于0
        assert result != null;
        int r = result.intValue();
        if (r != 0){
            //2.1 不等于0，记录异常，没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "用户不允许重复下单");
        }
        VoucherOrder voucherOrder = new VoucherOrder();
        //2.2 封装订单id
        voucherOrder.setId(orderId);
        //2.3 封装用户id
        voucherOrder.setUserId(userId);
        //2.4 封装优惠券id
        voucherOrder.setVoucherId(voucherId);
        //2.5 等于0，抢购的优惠券信息(优惠券id、用户id)存入到阻塞队列中
        ordersTasks.add(voucherOrder);
        //3. 获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();

        //4. 返回订单id
        return Result.ok(orderId);
    }

    /**
     * 同步秒杀下单
     */
//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        //1. 根据优惠券id查询优惠券信息
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
//
//        //2. 判断优惠券的日期是否生效
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            //如果没有生效
//            return Result.fail("尚未到抢购时间，请您稍后在来...");
//        }
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("本次活动已结束，请您关注后续...");
//        }
//        //3. 时间生效，判断库存是否充足
//        if (voucher.getStock() < 1) {
//            return Result.fail("很抱歉，本次活动已经抢完了");
//        }
//        // 5.一人下一单需求
//        Long userId = UserHolder.getUser().getId();
//        // 8. 在集群环境下 并发执行
//        //创建锁对象
//        SimpleRedisLock lock = new SimpleRedisLock(userId.toString(), stringRedisTemplate);
//        //获取锁
//        boolean isLock = lock.tryLock(1200L);
//        if (!isLock) {
//            return Result.fail("当前用户不允许重复下单！");
//        }
//        try {
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
//            //释放锁
//            lock.delLock();
//        }
//    }

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

    @Transactional
    @Override
    public void createVoucherOrderAysnc(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        //5.1 根据优惠券id和用户id查询订单
        Integer count = query()
                .eq("user_id", userId)
                .eq("voucher_id", voucherId)
                .count();
        //5.2 判断当前用户是否下单
        if (count > 0) {
            // 下单成功，已经下单返回已下单
            log.error("用户已经下过单了");
        }
        // 未下单，扣减库存
        boolean isSuccess = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        // 如果库存不足，返回错误
        if (!isSuccess) {
            log.error("库存不足，扣减失败");
        }
        //6. 保存订单
        save(voucherOrder);
    }


}
