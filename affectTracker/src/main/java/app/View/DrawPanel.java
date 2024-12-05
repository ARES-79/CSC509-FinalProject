package app.View;

import app.Data.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The {@code DrawPanel} class is a custom {@link JPanel} that visually represents
 * circles received from PropertyChangeEvents. Each circle is drawn with its respective
 * color, and an outline is added around each circle.
 * <p>
 * This panel is used as part of a graphical interface to display real-time visualizations
 * of the circles generated by the system. It ensures that each circle is displayed with
 * its fill color and a black border.
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class DrawPanel extends JPanel implements PropertyChangeListener {

	private final Logger log = LoggerFactory.getLogger(DrawPanel.class);
	private Deque<Circle> circleList = new ArrayDeque<>();

	public DrawPanel() {
		setBackground(Color.WHITE);
		setBorder(new MatteBorder(3, 3, 3, 3, Color.BLACK));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Circle c : circleList) {
			c.drawCircle(g);
		}
		// Set font and color
		g.setFont(new Font("Arial", Font.BOLD, 20));
		g.setColor(Color.BLUE);

		// Draw text
		g.drawString("Example Static Text on Panel!", 50, 100);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof Deque<?>){
			this.circleList = (Deque<Circle>) evt.getNewValue();
			repaint();
		} else {
			log.warn("Unexpected payload in PropertyChangeEvent: {}", evt.getNewValue());
		}

	}
}
