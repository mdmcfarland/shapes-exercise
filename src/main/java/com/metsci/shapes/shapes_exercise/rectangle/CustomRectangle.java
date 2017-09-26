package com.metsci.shapes.shapes_exercise.rectangle;

import java.util.logging.Logger;

import com.metsci.shapes.DraggablesMouseEvent;
import com.metsci.shapes.ShapeControl;
import com.metsci.shapes.xy.Box;
import com.metsci.shapes.xy.rectangle.Rectangle;

public class CustomRectangle extends Rectangle
{
	private static final Logger LOGGER = Logger.getLogger(CustomRectangle.class.getName());

	private double interiorX;
	private double interiorY;

	/// 3 constructors
	
	public CustomRectangle(Box box) {
		this(box, 0.3);
	}

	public CustomRectangle(Box box, double percentage) {
		this(box, box.xC + percentage * (box.xB-box.xC), box.yC + percentage * (box.yB - box.yC));
	}

	public CustomRectangle(Box box, double interiorX, double interiorY) {
		super(box);
		this.interiorX = interiorX;
		this.interiorY = interiorY;
		LOGGER.info("box corners:  " + box.xC + "," + box.yC + "   -  " + box.xB + "," + box.yB);
	}

	
	// getters
	public double getInteriorX() {
		return interiorX;
	}

	public double getInteriorY() {
		return interiorY;
	}
	
	public String toString() {
		return "<customRect"
				+ " interiorLoc=" + interiorX + "," + interiorY
				+ " />";
	}

	
	// setter
	public void setInteriorPoint(double x, double y) {
		this.interiorX = x;
		this.interiorY = y;
	}

	
	// update interior point to mouse position
	@Override
	public ShapeControl getControlAt(boolean selected, DraggablesMouseEvent ev) {
		if (this.contains(ev)) {
			setInteriorPoint(ev.x, ev.y);
		}
		return super.getControlAt(selected, ev);
	}
}
