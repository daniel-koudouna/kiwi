package com.proxy.kiwi.reader;

import java.util.Timer;
import java.util.TimerTask;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class ResizeListener implements ChangeListener<Number> {

	long delay;
	final Timer timer;
	TimerTask task;
	boolean started;

	public ResizeListener(long delay) {
		this.delay = delay;
		timer = new Timer();
		task = null;
		started = false;
	}

	public void changed(ObservableValue<? extends Number> observable, Number oldNum, Number newNum) {
		if (task != null) {
			task.cancel();
		}

		if (!started) {
			started = true;
			onResizeStart();
		}

		task = new TimerTask() {

			@Override
			public void run() {
				onResizeEnd();
				started = false;
			}

		};

		timer.schedule(task, delay);
	}

	public abstract void onResizeStart();

	public abstract void onResizeEnd();
}
