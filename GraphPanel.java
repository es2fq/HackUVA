import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.leapmotion.leap.*;
import javax.swing.SwingUtilities;
import java.awt.geom.Ellipse2D;

class GraphPanel extends JPanel {
    JFrame frame;
    
    public int width = 900;
    public int height = 600;
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private static final Stroke THIN_STROKE = new BasicStroke(2f);
    private static final Stroke THICK_STROKE = new BasicStroke(5f);
    private int pointWidth = 10;
    private int numberXDivisions = 10;
    private int numberYDivisions = 10;
    private List<Double> yCoords;
    private List<Double> xCoords;
    private List<Double> zCoords;

    public GraphPanel() {
        this.yCoords = new ArrayList<Double>();
        this.xCoords = new ArrayList<Double>();
    }

    public GraphPanel(List<Double> yCoords) {
        this.yCoords = yCoords;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        drawTable(g2);
        // create hatch marks and grid lines for y axis.
        // for (int i = 0; i < numberYDivisions + 1; i++) {
        //     int x0 = getWidth() / 2 + padding;
        //     int x1 = getWidth() / 2 + pointWidth + padding;
        //     int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
        //     int y1 = y0;
        //     if (yCoords.size() > 0) {
        //         g2.setColor(gridColor);
        //         g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
        //         g2.setColor(Color.BLACK);
        //         String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 - 26 + "";
        //         FontMetrics metrics = g2.getFontMetrics();
        //         int labelWidth = metrics.stringWidth(yLabel);
        //         g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
        //     }
        //     g2.drawLine(x0, y0, x1, y1);
        // }

        // create hatch marks and grid lines for x axis.
        for (int i = 0; i < numberXDivisions; i++) {
            int x0 = getWidth() - ((i * (getWidth() - padding * 2 - labelPadding)) / numberYDivisions + padding * 2 + labelPadding);
            int x1 = x0;
            int y0 = getHeight() - padding * 2;
            int y1 = getHeight() - pointWidth - padding * 2;
            if (yCoords.size() > 0) {
                // g2.setColor(gridColor);
                // g2.drawLine(padding + labelPadding + 1 + pointWidth, x0, getWidth() - padding, x1);
                g2.setColor(Color.BLACK);
                String xLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberXDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(xLabel);
                // g2.drawString(xLabel, x0 - labelWidth - 5, x0 + (metrics.getHeight() / 2) - 3);
            }
            // g2.drawLine(x0, y0, x1, y1); //draw hatches
        }

        
        // and for x axis
        for (int i = 0; i < yCoords.size(); i++) {
            if (yCoords.size() > 1) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (yCoords.size() - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((yCoords.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    // String xLabel = i + "";
                    // if (i == 0 || i == yCoords.size() - 1) {
                    //     xLabel = "";
                    // }
                    // FontMetrics metrics = g2.getFontMetrics();
                    // int labelWidth = metrics.stringWidth(xLabel);
                    // g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }
        drawControlInputs(g2);
        drawHands(g2);
        
    }
    private void drawControlInputs(Graphics2D g2) {
    	for (int a = 0; a < MidiControl.controlInputs.size(); a++) {
    		InputController.Handle controllingHandle = null;
    		MidiControl.ControlInput ci = MidiControl.controlInputs.get(a);
    		for (InputController.Handle handle : InputController.handles) {
    			if (handle.closestControlZone == a) {
    				controllingHandle = handle;
    				break;
    			}
    		}
			int x = ci.getCenterX();
			int y = ci.getCenterY();
			int z = ci.getCenterZ();
            g2.setColor(Color.RED);
            g2.setStroke(THIN_STROKE);
            boolean isBeingEdited = false;
            if (controllingHandle != null) {
            	//there is a handle either controlling or hovering over this control knob
                g2.setColor(Color.GREEN);
                drawLine(g2, x, y, z, controllingHandle.x, controllingHandle.y, controllingHandle.z);
                if (controllingHandle.pinchedInControlZone == a) {
                	isBeingEdited = true;
            	}	
            }
            if (isBeingEdited) {
            	ci.drawScale += (2 - ci.drawScale) * .1f;//then this is the knob currently being edited
            } else {
            	ci.drawScale += (1 - ci.drawScale) * .1f;
            	ci.clampValue();//reset the value so that it feels natural when edited again
            	//we dont clamp while editing so that if you twist past 0 or 1, it doesnt accidently go back to 0 < n < 1 because of small movements
            }
            Stroke outLine = new BasicStroke(3f * ci.drawScale);
    		if (ci instanceof MidiControl.ControlKnob) {
    			//draw the knob
                int knobSize = 40;
                int knobWidth = (int)(knobSize * getXFactor() * ci.drawScale);
                int knobHeight = (int)(knobSize * getYFactor() * ci.drawScale);
                g2.fillOval(getScreenX(x, y, z) - knobWidth / 2, getScreenY(x, y, z) - knobHeight / 2, knobWidth, knobHeight);
                g2.setFont(new Font("Calibri", Font.PLAIN, (int)(24 * ci.drawScale))); 
                String label = (""+(100*ci.clampedValue()));
                label = label.substring(0, Math.min(5, label.length()))+"%";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2.drawString(label, getScreenX(x, y, z) + knobWidth / 2, getScreenY(x, y, z) - metrics.getHeight() / 2);
                g2.setStroke(outLine);
                g2.setColor(Color.BLACK);
                g2.draw(new Ellipse2D.Double(getScreenX(x, y, z) - knobWidth / 2, getScreenY(x, y, z) - knobHeight / 2,
                		knobWidth,
                		knobHeight));
    		}
    		if (ci instanceof MidiControl.ControlSlider) {
    			//draw the knob
                int sliderWidth = (int) (20 * ci.drawScale);
                int sliderHeight = (int) (120 * ci.drawScale);
                g2.fillRect(getScreenX(x, y, z) - sliderWidth / 2, getScreenY(x, y, z) - sliderHeight / 2, sliderWidth, sliderHeight);
                g2.setFont(new Font("Calibri", Font.PLAIN, (int)(24 * ci.drawScale))); 
                String label = (""+(100*ci.clampedValue()));
                label = label.substring(0, Math.min(5, label.length()))+"%";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2.drawString(label, getScreenX(x, y, z) + sliderWidth / 2 + 5, getScreenY(x, y, z) - metrics.getHeight() / 2);
                Stroke baseLine = new BasicStroke(3f * ci.drawScale);
                g2.setStroke(outLine);
                g2.setColor(Color.BLACK);
                g2.drawRect(getScreenX(x, y, z) - sliderWidth / 2, getScreenY(x, y, z) - sliderHeight / 2, sliderWidth, sliderHeight);
    		}
    	}
    }
    private void drawLine(Graphics2D g2, double x1, double y1, double z1, double x2, double y2, double z2) {
        g2.drawLine(getScreenX(x1, y1, z1), getScreenY(x1, y1, z1), getScreenX(x2, y2, z2), getScreenY(x2, y2, z2));
    }
    private void drawHands(Graphics2D g2) {

        g2.setStroke(THICK_STROKE);
        
        for (InputController.Handle handle : InputController.handles) {
            double x = handle.x;
            double y = handle.y;
            double z = handle.z;
            int ovalW = 22;
            int ovalH = 16;

            g2.setColor(Color.BLACK);
            g2.drawLine(getScreenX(x, y, z), getScreenY(x, y, z), getScreenX(x, y, z), getScreenY(x, 0, z));
            for (Finger finger : handle.hand.fingers()) {
            	int boneNum = 0;
                for(Bone.Type boneType : Bone.Type.values()) {
                    Bone bone = finger.bone(boneType);
                    Vector base = bone.prevJoint();
                    Vector tip = bone.nextJoint();
                    g2.drawLine(getScreenX(base), getScreenY(base), getScreenX(tip), getScreenY(tip));
                    if (boneNum == 0) {
                        g2.drawLine(getScreenX(base), getScreenY(base), getScreenX(x, y, z), getScreenY(x, y, z));
                    }
                    boneNum++;
                }
            }

            if (handle.z > MidiControl.minZForInstruments) {
                g2.setColor(Color.BLUE);
            } else if (handle.z > MidiControl.maxZForControlZone) {
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(Color.GREEN);
            }
            g2.fillOval(getScreenX(x, y, z) - ovalW / 2, getScreenY(x, y, z) - ovalH / 2, ovalW, ovalH);
            
            if (handle.pinchedInControlZone != -1) {
            	handle.pinchDrawRadius += (2f - handle.pinchDrawRadius) * .2f;
            } else {
            	handle.pinchDrawRadius = 0;
            }
            if (handle.pinchDrawRadius > 0) {
            	g2.drawOval(getScreenX(x, y, z) - (int)(handle.pinchDrawRadius * ovalW / 2), getScreenY(x, y, z) - (int)(handle.pinchDrawRadius * ovalH / 2), (int)(handle.pinchDrawRadius * ovalW), (int)(handle.pinchDrawRadius * ovalH));
            }

            DecimalFormat df = new DecimalFormat(".#");
            String pointLabel = "(" + df.format(x) + ", " + df.format(y) + ")";
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(pointLabel);
            g2.drawString(pointLabel, getScreenX(x, y, z) - labelWidth / 2 - pointWidth, getScreenY(x, y, z));
        }
    }
    
    Polygon p, p2, p3, p4;
    
    private void drawTable(Graphics2D g2) {
    	if (p == null) {
	        p = new Polygon();
	        p2 = new Polygon();
	        p3 = new Polygon();
	        p4 = new Polygon();
	        int xPoints[] = {Core.worldXLeft, Core.worldXLeft, Core.worldXRight, Core.worldXRight};
	        int yPoints[] = {0, 0, 0, 0};
	        int zPoints[] = {Core.worldZFar, Core.worldZNear, Core.worldZNear, Core.worldZFar};
	
	        for (int i = 0; i < xPoints.length; i++) {
	            p.addPoint(getScreenX(xPoints[i], yPoints[i], zPoints[i]), getScreenY(xPoints[i], yPoints[i], zPoints[i]));
	            p2.addPoint(getScreenX(xPoints[i], yPoints[i] - 30, zPoints[i]), getScreenY(xPoints[i], yPoints[i] - 30, zPoints[i]));
	        }
	
	        p3.addPoint(getScreenX(xPoints[1], yPoints[1], zPoints[1]), getScreenY(xPoints[1], yPoints[1], zPoints[1]));
	        p3.addPoint(getScreenX(xPoints[1], yPoints[1] - 30, zPoints[1]), getScreenY(xPoints[1], yPoints[1] - 30, zPoints[1]));
	        p3.addPoint(getScreenX(xPoints[2], yPoints[2] - 30, zPoints[2]), getScreenY(xPoints[2], yPoints[2] - 30, zPoints[2]));
	        p3.addPoint(getScreenX(xPoints[2], yPoints[2], zPoints[2]), getScreenY(xPoints[2], yPoints[2], zPoints[2]));
	
	        p4.addPoint(getScreenX(xPoints[2], yPoints[2], zPoints[2]), getScreenY(xPoints[2], yPoints[2], zPoints[2]));
	        p4.addPoint(getScreenX(xPoints[2], yPoints[2] - 30, zPoints[2]), getScreenY(xPoints[2], yPoints[2] - 30, zPoints[2]));
	        p4.addPoint(getScreenX(xPoints[3], yPoints[3] - 30, zPoints[3]), getScreenY(xPoints[3], yPoints[3] - 30, zPoints[3]));
	        p4.addPoint(getScreenX(xPoints[3], yPoints[3], zPoints[3]), getScreenY(xPoints[3], yPoints[3], zPoints[3]));
    	}

//        g2.setColor(Color.GRAY);
//        g2.fillPolygon(p2);
        g2.setColor(Color.BLACK);
        g2.draw(p2);
        g2.setColor(Color.GRAY);
        g2.fillPolygon(p);
        g2.setColor(Color.BLACK);
        g2.draw(p);
        g2.setColor(Color.GRAY);
        g2.fillPolygon(p3);
        g2.setColor(Color.BLACK);
        g2.draw(p3);
        g2.setColor(Color.GRAY);
        g2.fillPolygon(p4);
        g2.setColor(Color.BLACK);
        g2.draw(p4);

        // for (int i = 0; i < graphPoints.size() - 1; i++) {
        //     int x1 = graphPoints.get(i).x;
        //     int y1 = graphPoints.get(i).y;
        //     int x2 = graphPoints.get(i + 1).x;
        //     int y2 = graphPoints.get(i + 1).y;
        //     g2.drawLine(x1, y1, x2, y2);
        // }
    }

    private int getScreenX(Vector v) {
    	return getScreenX(v.getX(), v.getY(), v.getZ());
    }
    
    private int getScreenY(Vector v) {
    	return getScreenY(v.getX(), v.getY(), v.getZ());
    }
    
    private double getXFactor() {
    	return .9;
    }
    
    private double getYFactor() {
    	return .6;
    }

    private int getScreenX(double x, double y, double z) {
        return (int) (getXFactor() * x - 0.25 * z)  + width / 2;
    }

    private int getScreenY(double x, double y, double z) {
        return (int) (height * 4 / 5 - getYFactor() * y + .35 * z);
    }

    private int getScreenZ(double x, double y, double z) {
        return (int) (z);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    
    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (Double score : yCoords) {
            minScore = Math.min(minScore, score);   
        }
        return 0;
        // return minScore;
    }
    
    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (Double score : yCoords) {
            maxScore = Math.max(maxScore, score);
        }
        return 170;
        // return maxScore;
    }
    
    public void update() {
    	invalidate();
    	this.repaint();
    }
    
}