package com.proxy.kiwi.core.v2.service;

import java.util.LinkedList;

/**
 * A generic {@link Service} to run Runnables in serial
 *
 */
public class GenericTaskService extends Service{

	static LinkedList<Runnable> folderTasks = new LinkedList<>();
	
	@Override
	protected void onStart() { }

	@Override
	protected void onRun() {
		if (folderTasks.isEmpty()) {
			rest(1000);
			return;
		}
		synchronized(folderTasks) {
			folderTasks.removeFirst().run();			
		}
	}

	@Override
	protected void onEnd() { }

	public static void enqueue(Runnable runnable) {
		synchronized (folderTasks) {
			folderTasks.add(runnable);
		}
	}

	public static int getQueueSize() {
		return folderTasks.size();
	}
}
