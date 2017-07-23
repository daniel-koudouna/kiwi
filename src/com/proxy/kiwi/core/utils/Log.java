package com.proxy.kiwi.core.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public enum Log {
	TIME("TIME"), GUI("GUI ", false), EVENT("EVNT"), IO("I /O"), PRELOAD("LOAD"), ERR("ERR ");

	private final String string;
	private final boolean visible;
	private static JFrame logwindow;
	private static TextArea log;
	private static final boolean DEBUG = System.getenv("KIWI_DEBUG") != null;
	private static final int MAX_STACK_DEPTH = 10;

	Log(String s) {
		this(s, true);
	}

	Log(String s, boolean v) {
		string = s;
		visible = v;
	}

	public String toString() {
		return string;
	}

	public boolean isVisible() {
		return visible;
	}

	public static void print(Log type, String message) {
		if (type.isVisible()) {
			String output = "[" + type + "]\t" + message;

			System.out.println(output);

			if (DEBUG) {
				if (logwindow == null) {
					initWindow();
				}
				if (log != null) {
					log.setText(log.getText() + output + "\n");
				}
			}
		}
	}

	public static void print(Exception e) {
		print(Log.ERR, e.getClass().getCanonicalName() + " on Thread " + Thread.currentThread().getName());
		if (e.getMessage() != null) {
			print(Log.ERR, e.getMessage());
		}
		for (int i = 0; i < MAX_STACK_DEPTH && i < e.getStackTrace().length; i++) {
			StackTraceElement el = e.getStackTrace()[i];
			print(Log.ERR, "\t...at " + el.getClassName() + "." + el.getMethodName()
					+ (el.getFileName() != null ? (" (" + el.getFileName() + ":" + el.getLineNumber()) + ")" : ""));

		}
	}

	public static void alert(Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.getMessage());
		for (int i = 0; i < MAX_STACK_DEPTH && i < e.getStackTrace().length; i++) {
			StackTraceElement el = e.getStackTrace()[i];
			sb.append("\n\t...at " + el.getClassName() + "." + el.getMethodName()
					+ (el.getFileName() != null ? (" (" + el.getFileName() + ":" + el.getLineNumber()) + ")" : ""));
		}

		JOptionPane.showConfirmDialog(null, sb.toString());
	}

	private static void initWindow() {
		logwindow = new JFrame();
		Container pane = logwindow.getContentPane();
		if (log == null) {
			log = new TextArea();
		}
		pane.add(log);
		logwindow.setSize(600, 600);
		logwindow.setVisible(true);
		logwindow.addWindowListener(new LogWindowListener());

	}

	static class LogWindowListener implements WindowListener {

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {
			logwindow = null;
		}
	}
}
