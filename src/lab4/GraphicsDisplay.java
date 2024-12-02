package lab4;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
	private Double[][] graphicsData;
	private Double[][] altGraphicsData;
	private float[] dash = { 3, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1};
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean showAltGraphic = false;
	private boolean isRotated = false;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private double scale;
	private BasicStroke graphicsStroke;
	private BasicStroke altGraphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
	private Font axisFont;

	public GraphicsDisplay() {
		setBackground(Color.WHITE);
		graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
		altGraphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		axisFont = new Font("Serif", Font.BOLD, 36);
	}

	public void showGraphics(Double[][] graphicsData) {
		this.graphicsData = graphicsData;

		repaint();
	}

	public void showAltGraphics(Double[][] altGraphicsData) {
		this.altGraphicsData = altGraphicsData;
		this.showAltGraphic = true;
		repaint();
	}

	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}
	
	public void setIsRotated(boolean isRotated) {
		this.isRotated = isRotated;
		repaint();
	}

	public void setShowAltGraphic(boolean showAltGraphic) {
		this.showAltGraphic = showAltGraphic;
		repaint();
	}

	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (graphicsData == null || graphicsData.length == 0)
			return;

		getScale();

		Graphics2D canvas = (Graphics2D) g;
		if(isRotated) {
			AffineTransform tform = canvas.getTransform();
			tform.quadrantRotate(-1,getSize().getWidth()/2, getSize().getHeight()/2);
			canvas.setTransform(tform);
		}
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		Font oldFont = canvas.getFont();
		if (showAxis)
			paintAxis(canvas);
		paintGraphics(canvas);
		if (showMarkers)
			paintMarkers(canvas);
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);
	}

	protected void getScale() {

		Double[] mmdata = getMinMax(graphicsData);

		minX = mmdata[0];
		maxX = mmdata[1];
		minY = mmdata[2];
		maxY = mmdata[3];

		if (this.showAltGraphic) {
			Double[] altmmdata = getMinMax(altGraphicsData);
			minX = Math.min(minX, altmmdata[0]);
			maxX = Math.max(maxX, altmmdata[1]);
			minY = Math.min(minY, altmmdata[2]);
			maxY = Math.max(maxY, altmmdata[3]);
		}
		double scaleX = getSize().getWidth() / (maxX - minX);
		double scaleY = getSize().getHeight() / (maxY - minY);
		scale = Math.min(scaleX, scaleY);
		if (scale == scaleX) {
			double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
			maxY += yIncrement;
			minY -= yIncrement;
		}
		if (scale == scaleY) {
			double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
			maxX += xIncrement;
			minX -= xIncrement;
		}

	}

	protected Double[] getMinMax(Double[][] graphicsData) {
		Double[] minmaxxy = {0.0,0.0,0.0,0.0};
		minmaxxy[0] = graphicsData[0][0];
		minmaxxy[1] = graphicsData[graphicsData.length - 1][0];
		minmaxxy[2] = graphicsData[0][1];
		minmaxxy[3] = minmaxxy[2];
		for (int i = 1; i < graphicsData.length; i++) {
			if (graphicsData[i][1] < minmaxxy[2]) {
				minmaxxy[2] = graphicsData[i][1];
			}
			if (graphicsData[i][1] > minmaxxy[3]) {
				minmaxxy[3] = graphicsData[i][1];
			}
		}
		return minmaxxy;
	}

	protected void paintGraphics(Graphics2D canvas) {
		canvas.setStroke(graphicsStroke);
		canvas.setColor(Color.RED);
		GeneralPath graphics = new GeneralPath();
		for (int i = 0; i < graphicsData.length; i++) {
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			if (i > 0) {
				graphics.lineTo(point.getX(), point.getY());
			} else {
				graphics.moveTo(point.getX(), point.getY());
			}
		}
		canvas.draw(graphics);
		
		if (showAltGraphic) {
			canvas.setStroke(altGraphicsStroke);
			canvas.setColor(Color.GREEN);
			GeneralPath altGraphics = new GeneralPath();
			for (int i = 0; i < altGraphicsData.length; i++) {
				Point2D.Double point = xyToPoint(altGraphicsData[i][0], altGraphicsData[i][1]);
				if (i > 0) {
					altGraphics.lineTo(point.getX(), point.getY());
				} else {
					altGraphics.moveTo(point.getX(), point.getY());
				}
			}
			canvas.draw(altGraphics);
		}
	}

	protected void paintMarkers(Graphics2D canvas) {
		canvas.setStroke(markerStroke);
		paintMarkersFromData(graphicsData,canvas);
		if(showAltGraphic) {
			paintMarkersFromData(altGraphicsData,canvas);
		}
	}

	protected void paintMarkersFromData(Double[][] graphicsData, Graphics2D canvas) {
		for (Double[] point : graphicsData) {
			canvas.setColor(Color.RED);
			if (sumLessThan10(point[1]))
				canvas.setColor(Color.BLUE);
			Ellipse2D.Double marker = new Ellipse2D.Double();
			Point2D.Double center = xyToPoint(point[0], point[1]);
			Point2D.Double corner = shiftPoint(center, 3, 3);
			Line2D.Double line1 = new Line2D.Double();
			Line2D.Double line2 = new Line2D.Double();
			marker.setFrameFromCenter(center, corner);
			line1.setLine(shiftPoint(center, 3, 0), shiftPoint(center, -3, 0));
			line2.setLine(shiftPoint(center, 0, 3), shiftPoint(center, 0, -3));
			canvas.draw(marker);
			canvas.draw(line1);
			canvas.draw(line2);
		}
		
	}
	
	protected boolean sumLessThan10(double y) {
		int sum = 0;
		int Y = (int) (y);
		if(Y < 0) Y = -Y;
		while (Y != 0) {
			sum += Y % 10;
			Y /= 10;
		}
		return sum < 10;
	}

	protected void paintAxis(Graphics2D canvas) {
		canvas.setStroke(axisStroke);
		canvas.setColor(Color.BLACK);
		canvas.setPaint(Color.BLACK);
		canvas.setFont(axisFont);
		FontRenderContext context = canvas.getFontRenderContext();
		if (minX <= 0.0 && maxX >= 0.0) {
			canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
			GeneralPath arrow = new GeneralPath();
			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
			arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
			arrow.closePath();
			canvas.draw(arrow);
			canvas.fill(arrow);
			Rectangle2D bounds = axisFont.getStringBounds("y", context);
			Point2D.Double labelPos = xyToPoint(0, maxY);
			canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
		}
		if (minY <= 0.0 && maxY >= 0.0) {
			canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
			GeneralPath arrow = new GeneralPath();
			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
			arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
			arrow.closePath();
			canvas.draw(arrow);
			canvas.fill(arrow);
			Rectangle2D bounds = axisFont.getStringBounds("x", context);
			Point2D.Double labelPos = xyToPoint(maxX, 0);
			canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10),
					(float) (labelPos.getY() + bounds.getY()));
		}
	}

	protected Point2D.Double xyToPoint(double x, double y) {
		double deltaX = x - minX;
		double deltaY = maxY - y;
		return new Point2D.Double(deltaX * scale, deltaY * scale);
	}

	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
		Point2D.Double dest = new Point2D.Double();
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}
}