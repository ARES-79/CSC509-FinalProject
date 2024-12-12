package app.Data;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import app.Model.Blackboard;

public class Highlight {
   private int xCoord;
   private int yCoord;
   private Color color;
   private int length;
   private int rowSize = Blackboard.getInstance().getRowSize();
   private final float opacity = 0.5f;

   public Highlight(int xCoord, int yCoord, Color color, int length) {
      this.xCoord = xCoord;
      // this.yCoord = roundToNearestRowSize(yCoord) + 50;
      this.yCoord = yCoord;
      this.color = color;
      this.length = length;
   }

   private int roundToNearestRowSize(int yCoord) {
      return (int) (Math.round((double) yCoord / (rowSize)) * (rowSize));
   }

   public int getX() {
      return xCoord;
   }

   public Color getColor() {
      return color;
   }

   public int getLength() {
      return length;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public int getY() {
      return yCoord;
   }

   public void setX(int xCoord) {
      this.xCoord = xCoord;
   }

   public void setY(int yCoord) {
      this.yCoord = yCoord;
   }

   public void increaseLength(int increment) {
      // increase length from current position (offset x)
      this.xCoord += increment;
      this.length += increment;
   }

   public void drawHighlight(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;

      // Save the original composite
      AlphaComposite originalComposite = (AlphaComposite) g2d.getComposite();

      // Set the opacity for the highlight
      AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
      g2d.setComposite(alphaComposite);

      g2d.setColor(color);
      g2d.fillRect(xCoord - (length / 2), yCoord, length, rowSize / 3);

      // Reset the composite to the original
      g2d.setComposite(originalComposite);
   }
}
