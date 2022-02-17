package frc.io.joysticks.util;
/*
Original Author: Sherya
Rewite Author: Jim Hofmann
History:
JCH - 11/6/2019 - rework
S - 3/6/2017 - Original release
TODO: more testing.  maybe add an array handler?
Desc: Allows use of various joystick/gamepad configurations.
Constructor get JS_ID and axisID.  However, if the it needs to pass a default (axis may not
exist for some combinations) then in axisID pass 10 * default value + 100 ( 10 * -default - 100).
IE., to default 0.0 pass 100.  For -1.0 pass -110.  For 1.0 pass 110.
*/

import edu.wpi.first.wpilibj.Joystick;

public class Axis{
	
	private Joystick joystick;
	private int axisID;
	private boolean exists;
	private double exDefault;
	
	// Constructor, normal
	// Exists muxed with axisID, if GT 100 (LT 0) does not exist
	public Axis(Joystick injoystick, int inaxisID) {
		joystick = injoystick;
		axisID = inaxisID;
		exists = joystick != null;
		exDefault = 0;	// default to 0
	}

	// Constructor, defaults set to does not exist & 0.0
	public Axis() {
		this.exists = false;
		this.exDefault = 0.0;
	}

	// Constructor, defaults set to does not exist & passed value
	public Axis(double exDefault) {
		this.exists = false;
		this.exDefault = exDefault;
	}

	// assign a new joystick & button
	//inAxisID: 0 = x axis, 1 = y axis
	public void setAxis(Joystick injoystick, int inAxisID){
		joystick = injoystick;
		axisID = inAxisID;
		exists = joystick != null;
		exDefault = 0;
	}

	/**Clear assignment.  Joystick = null & axisID = 0. */
	public void setAxis(){
		setAxis(null, 0);
	}

	// get the axis value
	public double get() {
		return exists ? joystick.getRawAxis(axisID) : exDefault;
	}
}