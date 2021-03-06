package com.nicky.controller;

import com.nicky.redis.RedisUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CountDownLatch;

@RestController
public class TestController {

    private volatile Integer sucNumber = 0;
    private volatile Integer failNumber = 0;
    Jedis jedis;

    @RequestMapping(value = "")
    public String index() {
        return "welcome to seckill";
    }
    @RequestMapping(value = "/test")
    public String testSecKill() {
        RedisUtil redisUtil = RedisUtil.getInstance();

        int threadCount = 1000;
        final CountDownLatch endCount = new CountDownLatch(threadCount);
        final CountDownLatch beginCount = new CountDownLatch(1);

        //redis 设置秒杀某商品剩余库存数
        String commidityName = "apple";
        String commidityNumber = "0";
        //先判断时间到点没，没到点直接返回
        //判断时间是否到了秒杀时间
        if(!redisUtil.exists(commidityName)){
            //不存在 就去数据去里面去查询再set进redis，并设置个过期时间
            redisUtil.set(commidityName, commidityNumber);
            redisUtil.expire(commidityName, 200000);
        }else {
            redisUtil.del(commidityName);
            redisUtil.set(commidityName, commidityNumber);
            redisUtil.expire(commidityName, 200000);
        }

        Thread[] threads = new Thread[threadCount];
        //起500个线程，秒杀第一个商品
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        //等待在一个信号量上，挂起
                        beginCount.await();
                        //直接秒杀库存
                        String currentThreadName = Thread.currentThread().getName();
                        secKill(currentThreadName, commidityName,redisUtil);
                        endCount.countDown();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            threads[i].start();

        }



        long startTime = System.currentTimeMillis();
        //主线程释放开始信号量，并等待结束信号量
        beginCount.countDown();
        try {
            //主线程等待结束信号量
            endCount.await();
            //观察秒杀结果是否正确
            System.out.println("成功秒杀的人数:"+sucNumber);
            System.out.println("没有秒杀到的人数:"+failNumber);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "success";
    }

    //秒杀主方法
    public void secKill(String theadName, String commidityName,RedisUtil redisUtil) {
        //某商品秒杀只能一件
        try {
            Long remainNumber = redisUtil.incr(commidityName);
            Integer intNumber = remainNumber.intValue()+1;
            if (intNumber >0) {
                System.out.println("线程名:" + theadName + "，秒杀了第" + intNumber + "号商品");
                sucNumber++;
                //业务操作 ;
            } else {
                System.out.println("抱歉！线程名:" + theadName + "商品已经秒杀完了");
                failNumber++;
                //业务操作  ;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("redis 异常");
        }

    }
}
