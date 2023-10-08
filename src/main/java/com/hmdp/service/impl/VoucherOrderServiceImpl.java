package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
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

    /**
     * 秒杀下单
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        //1. 根据优惠券id查询优惠券信息
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        //2. 判断优惠券的日期是否生效
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
            //如果没有生效
            return Result.fail("尚未到抢购时间，请您稍后在来...");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())){
            return Result.fail("本次活动已结束，请您关注后续...");
        }
        //3. 时间生效，判断库存是否充足
        if (voucher.getStock() < 1){
            return Result.fail("很抱歉，本次活动已经抢完了");
        }
        //如果库存充足，先扣减库存
        /**
         * 解决线程不安全问题：乐观锁（CAS）WHERE (voucher_id = ? and stock = ?)
         *                     WHERE (voucher_id = ? and stock > 0)
         */
        boolean isSuccess = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).gt("stock", 0)
                .update();
        // 如果库存不足，返回错误
        if (!isSuccess){
            return Result.fail("库存不足！");
        }
        //4. 新建一个订单并返回订单id
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId("voucher_order");
        voucherOrder.setId(orderId);
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        //5. 保存订单
        save(voucherOrder);
        return Result.ok(orderId);
    }


}
