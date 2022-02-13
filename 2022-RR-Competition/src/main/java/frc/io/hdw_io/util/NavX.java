package frc.io.hdw_io.util;


import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;

/* TODO:
*  This code is functional but I have some standards questions.
*  There can only be one NavX, it plugs into the roboRIO.  These should all be static?
*  Why is there a AHRS method?  It returns this.object.  Why not a constructor?  As static
*  it would be a copy of this but done in a normal construct.
*/

/**
 * Constructor for a Kauailabs navX, gyro, object.
 */
public class NavX {
	/**
	 * Declare a variable using SPI connector
	 */
	public AHRS ahrs = new AHRS(SPI.Port.kMXP);
	
	/**
	 * @return raw Z position.  No limit, +/-360 (or +/-180).
	 */
	public double getAngle() {
		return ahrs.getAngle();
	}

	/**
	 * @param angle to be normalized 0 to 360.
	 * @return a value between 0 to 360.
	 */
	public double normalizedAngle( double angle ) {
		return ((angle %  360) + 360) % 360;		//????
	}

	/**
	 * @return normalized navX Z position, gyro, between 0 and 360 degrees
	 */
	public double getNormalizedAngle() {
		return (normalizedAngle(ahrs.getAngle()));
	}

    /**
     * Normalize angle between -180 & 180 continuously
     * 
     * @param angle  Angle to evaluate
     * @return Noralized angle from -180 to +180, continuously
     */
    public static double normalizeTo180( double angle){
        double tmpD = angle % 360.0;  //Modulo 0 to 360
        if( tmpD < -180.0 ){    //If LT -180 add 360 for complement angle
            tmpD += 360.0;
        }else if(tmpD > 180){   //If GT +180 substract 360 for complement angle
            tmpD -= 360;
        }
        return tmpD;
    }

	/**
	 * @return normalized navX Z position, gyro, between -180 and 180 degrees
	 */
	public double getNormalizeTo180() {
		return (normalizedAngle(ahrs.getAngle()));
	}

	/**
	 * Set navX z position, gyro, to 0 degrees.
	 */
	public void reset() {
		ahrs.reset();
	}

	/**
	 * @return navX ID object variable(?).
	 */
	public AHRS getAHRS() {
		return ahrs;
	}

}