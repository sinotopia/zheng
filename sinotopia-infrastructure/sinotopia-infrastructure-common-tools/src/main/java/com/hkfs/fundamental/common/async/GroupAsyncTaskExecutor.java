package com.hkfs.fundamental.common.async;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author brucezee 2013-5-25 下午2:01:20
 */
public class GroupAsyncTaskExecutor {
	private static final long TIMEOUT_MILLISECONDS = 15000;
	private List<GroupSafeRunnable> taskList = new LinkedList<GroupSafeRunnable>();
	private long timeout = 0;
	
	private AsyncTaskPool asyncTaskPool;
	
	public GroupAsyncTaskExecutor(AsyncTaskPool asyncTaskPool) {
		this.asyncTaskPool = asyncTaskPool;
	}
	
	public void addTask(GroupSafeRunnable task) {
		if (task != null) {
			taskList.add(task);
		}
	}
	
	public long getTimeout() {
		return timeout != 0 ? timeout : TIMEOUT_MILLISECONDS;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void start() {
		if (taskList.size() > 0) {
			CountDownLatch countDownLatch = new CountDownLatch(taskList.size());
			for (GroupSafeRunnable task : taskList) {
				//注入
				task.setCountDownLatch(countDownLatch);
				sendAsyncTask(task);
			}
			try {
				countDownLatch.await(getTimeout(), TimeUnit.MILLISECONDS);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void sendAsyncTask(SafeRunnable runnable) {
		if (asyncTaskPool != null) {
			try {
				asyncTaskPool.sendTask(runnable);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
