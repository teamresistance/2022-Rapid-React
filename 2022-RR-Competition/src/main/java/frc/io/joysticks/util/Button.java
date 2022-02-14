package frc.io.joysticks.util;
/*
Original Author: Sherya
Rewite Author: Jim Hofmann
History:
JCH - 11/6/2019 - rework
S - 3/6/2017 - Original release
TODO: more testing.  maybe add an array handler?
Desc: Allows use of various joystick/gamepad configurations.
Constructor get JS_ID and axisID.  However, if the it needs to pass a default (button may not
exist for some combinations) then in buttonID pass 100 (even) for true, 101 (odd for false).
*/

import edu.wpi.first.wpilibj.Joystick;

public class Button{
	
	private Joystick joystick;
	public int buttonID;
	private boolean exists;
	private boolean existDflt;
	
	// Constructor, normal
	// Exists muxed with axisID, if GT 100 (LT 0) does not exist
	public Button(Joystick injoystick, int inbuttonID) {
		joystick = injoystick;
		buttonID = inbuttonID;
		exists = joystick != null;
		existDflt = buttonID != -1 ? false : true;	// default to false, if -1 default to true
	}

	// Constructor, defaults set to does not exist & false
	public Button() {
		exists = false;
		existDflt = false;
	}

	// Constructor, defaults set to does not exist & passed value
	public Button(boolean exDefault) {
		exists = false;
		existDflt = exDefault;
	}

	// assign a new joystick & button
	public void setButton(Joystick injoystick, int inbuttonID){
		joystick = injoystick;
		buttonID = inbuttonID;
		exists = joystick != null;
		existDflt = buttonID != -1 ? false : true;	// default to false, if -1 default to true
	}

	// Clear assignment.  Joystick = null & buttonID = 0.
	public void setButton(){
		setButton(null, 0);
	}

	// get current value
	public boolean isDown() {
		return exists ? joystick.getRawButton(buttonID) : existDflt;
	}

	// inverse of the current value
	public boolean isUp() {
		return exists ? !joystick.getRawButton(buttonID) : !existDflt;
	}

	// returns true once when button pressed
	public boolean onButtonPressed() {
		return exists ? joystick.getRawButtonPressed(buttonID) : existDflt;
	}

	// returns true once when button is released
	public boolean onButtonReleased() {
		return exists ? joystick.getRawButtonReleased(buttonID) : existDflt;
	}
}