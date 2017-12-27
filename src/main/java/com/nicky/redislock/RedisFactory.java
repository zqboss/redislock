package com.nicky.redislock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
		String ip = "192.168.0.88";
		int port = 6379;
		JedisPoolConfig config = new JedisPoolConfig();
		// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
		// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
		config.setMaxTotal(10000);
		// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
		config.setMaxIdle(2000);
		// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
		config.setMaxWaitMillis(1000 * 100);
		config.setTestOnBorrow(true);
		pool = new JedisPool(config, ip, port, 100000, "zengqiao");
		RedisClient client = new RedisClient(pool);
		return client;
	}
}
