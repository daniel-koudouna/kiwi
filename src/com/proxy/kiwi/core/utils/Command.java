package com.proxy.kiwi.core.utils;

import javafx.scene.input.KeyCode;

public enum Command {

	UP("Up",KeyCode.UP,KeyCode.W),
	DOWN("Down",KeyCode.DOWN,KeyCode.S),
	LEFT("Left",KeyCode.LEFT,KeyCode.A),
	RIGHT("Right",KeyCode.RIGHT,KeyCode.D),
	ENTER("Enter",KeyCode.ENTER),
	BACK("Back",KeyCode.BACK_SPACE),
	FULL_SCREEN("Full Screen",KeyCode.F),
	MINIMIZE("Minimize",KeyCode.M),
	EXIT("Exit",KeyCode.X),
	ZOOM_IN("Zoom In",KeyCode.EQUALS),
	ZOOM_OUT("Zoom Out",KeyCode.MINUS),
	CHAPTER_NEXT("Next Chapter",KeyCode.E),
	CHAPTER_PREVIOUS("Previous Chapter",KeyCode.Q),
	CHAPTER_ADD("Add Chapter",KeyCode.COMMA),
	CHAPTER_REMOVE("Remove Chapter",KeyCode.PERIOD),
	NEXT_FOLDER("Next Folder", KeyCode.L),
	PREVIOUS_FOLDER("Previous Folder", KeyCode.K),
	QUALITY("Change Quality", KeyCode.B),
	OPTIONS("Options", KeyCode.ALT),
	
	UNDEFINED("Undefined");
	
	public final String[] default_hotkeys;
	final String name;

	private Command(String name, KeyCode... hotkeys) {
		this.name = name;
		default_hotkeys = new String[hotkeys.length];
		for (int i = 0; i < hotkeys.length; i++) {
			default_hotkeys[i] = hotkeys[i].getName();
		}
	}

	public String getName() {
		return name;
	}

	public static Command get(String name) {
		for (Command command : values()) {
			if (command.getName().equals(name)) {
				return command;
			}
		}

		return Command.UNDEFINED;
	}
}
