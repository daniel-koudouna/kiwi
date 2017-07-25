package com.proxy.kiwi.core.v2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Service class is an extension of the {@link Thread} class focusing on singletons, which adds 
 * a number of quality of life improvements, most notably an automatic loading and unloading system. 
 * Threads implementing the Service class can also be easily restarted and terminated, and offer a stricter
 * execution split in three steps: The initialization, the main body and the cleanup.
 * @author Daniel
 *
 */
public abstract class Service extends Thread{
	
	/**
	 * The list of services currently running.
	 */
	private static List<Service> activeServices = new ArrayList<>();

	/**
	 * Starts a number of sevices. Services already started are unaffected.
	 * @param services The classes of all the services to be started.
	 */
	@SafeVarargs
	public static void init(Class<? extends Service>...services) {
		for (Class<? extends Service> clazz : services) {
			if (Service.class.equals(clazz)) {
				continue;
			}
			
			boolean running = activeServices.stream().anyMatch(s -> s.getClass().equals(clazz));
			
			if (running) {
				continue;
			}

			try {
				Service service = clazz.newInstance();
				activeServices.add(service);
				service.start();
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
	}

	public static boolean isRunning(Class<? extends Service> service) {
		return activeServices.stream().anyMatch(s -> s.getClass().equals(service));
	}
	
	/**
	 * Stops a number of services. Services not started are unaffected.
	 * @param services The classes of all the services to be stopped.
	 */
	@SafeVarargs
	public static void stop(Class<? extends Service>...services) {
		Optional<Service> service = Optional.empty();
		for (Class<? extends Service> clazz : services) {
			service = activeServices.stream().filter(s -> s.getClass().equals(clazz)).findAny();
			
			if (service.isPresent()) {
				break;
			}
		}
		
		if (service.isPresent()) {
			service.get().stopService();
			activeServices.remove(service.get());
		}
	}
	
	private boolean isRunning;
	
	public void run() {
		onStart();
		isRunning = true;
		while (isRunning) {
			onRun();
		}
		onEnd();
	}

	/**
	 * Instructs the service to join at the next possible opportunity. This method is deliberately unavailable
	 * to subclasses, and must be invoked from the {@link Service} superclass itself.
	 * 
	 * @see Service#stop(Class...)
	 */
	private final void stopService() {
		isRunning = false;
	}
	
	/* Obligatory wrapper for the sleep method */
	protected final void rest(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected abstract void onStart();
	protected abstract void onRun();
	protected abstract void onEnd();
	
}
