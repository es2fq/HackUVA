import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class GraphPanel extends JPanel {
    JFrame frame;
    
    private int width = 800;
    private int heigth = 400;
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
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
        
        // double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (yCoords.size() - 1);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);
        
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
        

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        
        g2.setStroke(oldStroke);
        for (int i = 0; i < xCoords.size(); i++) {
            double x = xCoords.get(i) + 430;
            double y = yCoords.get(i);
            double z = zCoords.get(i);
            int ovalW = pointWidth;
            int ovalH = pointWidth;

            g2.setColor(Color.BLACK);
            g2.drawLine(getScreenX(x, y, z), getScreenY(x, y, z), getScreenX(x, y, z), getScreenY(x, getHeight(), z));

            g2.setColor(pointColor);
            g2.fillOval(getScreenX(x, y, z) - ovalW / 2, getScreenY(x, y, z) - ovalH / 2, ovalW, ovalH);

            DecimalFormat df = new DecimalFormat(".#");
            String pointLabel = "(" + df.format(xCoords.get(i)) + ", " + df.format(yCoords.get(i)) + ")";
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(pointLabel);
            g2.drawString(pointLabel, getScreenX(x, y, z) - labelWidth / 2 - pointWidth, getScreenY(x, y, z));
        }
        
        // create x and y axes 
        // g2.drawLine(getWidth() / 2 + padding, getHeight() - padding - labelPadding, getWidth() / 2 + padding, padding);

        //bottom 2 lines of box
        // g2.drawLine(padding + labelPadding + 50, getHeight() - padding * 2 - labelPadding - 7, getWidth() - padding - 100, getHeight() - padding * 2 - labelPadding - 7);
        // g2.drawLine(padding + labelPadding + 50, getHeight() - padding * 2 - labelPadding - 27, getWidth() - padding - 100, getHeight() - padding * 2 - labelPadding - 27);

        //connecting bottom 2 lines
        // g2.drawLine(padding + labelPadding + 50, getHeight() - padding * 2 - labelPadding - 7, padding + labelPadding + 50, getHeight() - padding * 2 - labelPadding - 27);
        // g2.drawLine(getWidth() - padding - 100, getHeight() - padding * 2 - labelPadding - 7, getWidth() - padding - 100, getHeight() - padding * 2 - labelPadding - 27);

        //diagonal lines
        // g2.drawLine(padding + labelPadding + 50, getHeight() - padding * 2 - labelPadding - 27, padding + labelPadding + 50 + 75, getHeight() - padding * 2 - labelPadding - 27 - 50);

        // side of box
        Polygon p = new Polygon();
        int xPoints[] = {padding + labelPadding + 50, padding + labelPadding + 50, getWidth() - padding - 100, getWidth() - padding - 100};
        int yPoints[] = {getHeight() - padding * 2 - labelPadding - 7, getHeight() - padding * 2 - labelPadding - 27, getHeight() - padding * 2 - labelPadding - 27, getHeight() - padding * 2 - labelPadding - 7};
        for (int i = 0; i < xPoints.length; i++) {
            p.addPoint(xPoints[i], yPoints[i]);
        }
        g2.setColor(Color.BLACK);
        g2.draw(p);
        g2.setColor(Color.GRAY);
        g2.fillPolygon(p);

        // top of box
        Polygon p2 = new Polygon();
        int xPoints2[] = {padding + labelPadding + 50, padding + labelPadding + 50 + 75, getWidth() - padding - 25, getWidth() - padding - 100};
        int yPoints2[] = {getHeight() - padding * 2 - labelPadding - 27, getHeight() - padding * 2 - labelPadding - 27 - 50, getHeight() - padding * 2 - labelPadding - 27 - 50, getHeight() - padding * 2 - labelPadding - 27};
        for (int i = 0; i < xPoints2.length; i++) {
            p2.addPoint(xPoints2[i], yPoints2[i]);
        }
        g2.setColor(Color.BLACK);
        g2.draw(p2);
        g2.setColor(Color.GRAY);
        g2.fillPolygon(p2);

        // right of box
        Polygon p3 = new Polygon();
        int xPoints3[] = {getWidth() - padding - 25, getWidth() - padding - 25, getWidth() - padding - 100, getWidth() - padding - 100};
        int yPoints3[] = {getHeight() - padding * 2 - labelPadding - 27 - 50, getHeight() - padding * 2 - labelPadding - 7 - 50, getHeight() - padding * 2 - labelPadding - 7, getHeight() - padding * 2 - labelPadding - 27};
        for (int i = 0; i < xPoints3.length; i++) {
            p3.addPoint(xPoints3[i], yPoints3[i]);
        }
        g2.setColor(Color.BLACK);
        g2.draw(p3);
        g2.setColor(Color.GRAY);
        g2.fillPolygon(p3);

        g2.setColor(Color.BLACK);
        for (int i = 0; i < xPoints.length - 1; i++) {
            g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            g2.drawLine(xPoints2[i], yPoints2[i], xPoints2[i + 1], yPoints2[i + 1]);
            g2.drawLine(xPoints3[i], yPoints3[i], xPoints3[i + 1], yPoints3[i + 1]);
        }
        // for (int i = 0; i < graphPoints.size() - 1; i++) {
        //     int x1 = graphPoints.get(i).x;
        //     int y1 = graphPoints.get(i).y;
        //     int x2 = graphPoints.get(i + 1).x;
        //     int y2 = graphPoints.get(i + 1).y;
        //     g2.drawLine(x1, y1, x2, y2);
        // }
    }

    private int getScreenX(double x, double y, double z) {
        return (int) (x - 0.5 * z);
    }

    private int getScreenY(double x, double y, double z) {
        return (int) (200 - y + 0.7 * z);
    }

    private int getScreenZ(double x, double y, double z) {
        return (int) (z);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, heigth);
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
<<<<<<< HEAD

    public void setData(List<Double> yCoords, List<Double> xCoords, List<Double> zCoords) {
        this.yCoords = yCoords;
        this.xCoords = xCoords;
        this.zCoords = zCoords;
=======
    
    public void setScores(List<Double> scores) {
        this.scores = scores;

        this.scores.add(0, 0.0);
        this.scores.add(0.0);

>>>>>>> d665c14947fc4d295f147b169b775b05ef707c0b
        invalidate();
        this.repaint();
    }
    
    public List<Double> getScores() {
        return yCoords;
    }
    
    public void update() {
        List<Double> currScores = new ArrayList<Double>();
        List<Double> currXValues = new ArrayList<Double>();
        List<Double> currZValues = new ArrayList<Double>();
        List<MidiControl.HandleMusician> handleMusicians = MidiControl.handleMusicians;
        for (MidiControl.HandleMusician hm : handleMusicians) {
            currScores.add((double) hm.currentPitch);
            currXValues.add(hm.pitchHandle.x);
            currZValues.add(hm.pitchHandle.z);
        }
        setData(currScores, currXValues, currZValues);
    }
}