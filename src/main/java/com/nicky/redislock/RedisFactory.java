package com.nicky.redislock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.nicky.redis.RedisUtil;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisFactory {

	public static JedisPoolConfig getPoolConfig() throws IOException{
		Properties properties = new Properties();
		
		InputStream in = RedisFactory.class.getClassLoader().getResourceAsStream("redis.properties");
		
		try {
			properties.load(in);
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(Integer.parseInt(properties.getProperty("maxIdle", "100")));
			config.setMinIdle(Integer.parseInt(properties.getProperty("minIdle", "1")));
			config.setMaxTotal(Integer.parseInt(properties.getProperty("maxTotal", "1000")));
			return config;
		} finally {
			in.close();
		}
		
	}
	
	public static RedisClient getDefaultClient(){
		JedisPool pool = null;
		String ip = "10.28.200.248";
		int port = 6379;
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(200);
		config.setMaxIdle(50);
		config.setMinIdle(8);//设置最小空闲数
		config.setMaxWaitMillis(10000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		//Idle时进行连接扫描
		config.setTestWhileIdle(true);
		//表示idle object evitor两次扫描之间要sleep的毫秒数
		config.setTimeBetweenEvictionRunsMillis(30000);
		//表示idle object evitor每次扫描的最多的对象数
		config.setNumTestsPerEvictionRun(10);
		//表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
		config.setMinEvictableIdleTimeMillis(60000);
		pool = new JedisPool(config, ip, port, 100000, RedisUtil.PASSWORD);
		RedisClient client = new RedisClient(pool);
		return client;
	}
}
