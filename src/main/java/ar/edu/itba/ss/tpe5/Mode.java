package ar.edu.itba.ss.tpe5;

import java.util.Arrays;
import java.util.Optional;

public enum Mode {
	SINGLE_RUN (0), 
	COMMON_TEST (1),
	ETA_TEST (2),
	DENSITY_TEST (3);
	
	private int mode;
	
	Mode(int mode) {
		this.mode = mode;
	}
	
	public int getMode() {
		return mode;
	}
	
	public static Optional<Mode> valueOf(int value) {
		return Arrays.stream(values()).filter(m -> m.getMode() == value).findFirst();
	}
}
