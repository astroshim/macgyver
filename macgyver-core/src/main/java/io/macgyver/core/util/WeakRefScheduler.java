package io.macgyver.core.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * WeakRefScheduler functions like a ScheduledThreadPoolExecutor except that the
 * scheduled Runnable instance is held as a WeakReference.
 * @author rschoening
 *
 */
public class WeakRefScheduler {

	public static final int DEFAULT_POOL_SIZE=3;
	
	static Logger logger = LoggerFactory.getLogger(WeakRefScheduler.class);
	
	ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	
	public WeakRefScheduler() {
		this(DEFAULT_POOL_SIZE);
	}
	
	public WeakRefScheduler(int corePoolSize) {
		ThreadFactory tf = new ThreadFactoryBuilder().setDaemon(true).build();
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize,tf,new RejectedHandler());
	}

	public static class Task implements Runnable {
		
		WeakRefScheduler scheduler;
		WeakReference<Runnable> ref;
		
		public Task(WeakRefScheduler scheduler, Runnable r) {
			this.scheduler = scheduler;
			this.ref = new WeakReference<Runnable>(r);
		
		}

		@Override
		public void run() {
			Runnable r = ref.get();
			if (r==null) {
				logger.debug("descheduling {}",this);
				scheduler.scheduledThreadPoolExecutor.remove(this);
			}
			else {
				r.run();
			}
			
		}
	}
	
	public  class RejectedHandler implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			logger.warn("rejected execution of {}",r);
			
		}
		
	}
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, int initialDelay, int delay, TimeUnit timeUnit) {
		Task t = new Task(this,r);
		return scheduledThreadPoolExecutor.scheduleAtFixedRate(t, initialDelay, delay, timeUnit);
	}
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable r, int initialDelay, int delay, TimeUnit timeUnit) {
		Task t = new Task(this,r);
		return scheduledThreadPoolExecutor.scheduleWithFixedDelay(t, initialDelay, delay, timeUnit);
	}
}
