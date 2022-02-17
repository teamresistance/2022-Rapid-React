package frc.io.hdw_io.util;
/*
Author: Shreya
History:
JCH - 11/8/2019 - added additional functions.  isActive/Deactive & onAactive/Deactive
S - 11/8/2019 - Original Release

Desc:
Sets the feedback from a digital input to normally closed.
*/

import edu.wpi.first.wpilibj.DigitalInput;

/**
 * Creates a DigitalIput that can return an invarted status.
 */
public class InvertibleDigitalInput {
    private DigitalInput limitSwitch;
    private boolean isInverted;
    private boolean previousState;

    /**Construct for an Object and sets it to either inverted or normal. */
    public InvertibleDigitalInput(int channel, boolean invert) {
        isInverted = invert;
        limitSwitch = new DigitalInput(channel);
    }

    /**
     * @return the current state.  If isInverted then inverted state.
     */
    public boolean get() {
        return (isInverted ^ limitSwitch.get());
        // return (isInverted ? !limitSwitch.get() : limitSwitch.get());
    }

    /**
     * @return true if current state is false.
     */
    public boolean isActive() {
    // public boolean isActive(boolean currentState) {
        return get();
    }	
    
    /**
     * @return true if current state is false.
     */
    public boolean isDeactive() {
	// public boolean isDeactive(boolean currentState) {
		return !get();
	}	
	
    /**
     * @return true if state has changed to true.
    */
	public boolean onActive() {
        if(get() != previousState){
            previousState =  get();
            return true;
        }else{
            return false;
        }
	}
    
    /**
     * @return true if state has changed to false.
     */
	public boolean onDeactive() {
        if(!get() != previousState){
            previousState =  get();
            return true;
        }else{
            return false;
        }
	}
}
