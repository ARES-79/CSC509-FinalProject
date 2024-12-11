package app.Data;

import java.awt.*;


/**
 * Enum representing various emotions that can be processed.
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public enum Emotion {
 
	NONE (Color.GRAY, -1),

	//attention, engagement, excitement, stress, relaxation, interest
	ATTENTION (Color.YELLOW, 0),
	ENGAGEMENT (Color.GREEN, 1),
	EXCITEMENT (Color.BLUE, 2),
	STRESS (Color.RED, 3),
	RELAXATION (Color.MAGENTA, 4),
	INTEREST (Color.CYAN, 5);
	
	private final Color color;
	private final int value;
	
	Emotion(Color color, int value) {
		this.color = color;
		this.value = value;
	}
	
	public Color getColor() {
		return color;
	}
	
	public int getValue() {
		return value;
	}
	
	public static Emotion getByValue(int number) {
		for (Emotion e : Emotion.values()) {
			if (e.getValue() == number) {
				return e;
			}
		}
		return null;
	}
	
}