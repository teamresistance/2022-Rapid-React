package frc.io.hdw_io.util;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;

/**
 * Adds functions to Kauailabs navX, gyro, object.
 */
public class NavX extends AHRS {
	/**
	 * Constructor for AHRS 
	 * <p>public AHRS ahrs = new AHRS(SPI.Port.kMXP);
	 */
	public NavX(SPI.Port spiPort){
		super(spiPort);
	}
	
	/**@return getAngle() value normalized between 0 to 360. */
	public double getNormalizedAngle() {
		return normalizeAngle(this.getAngle());
	}

	/**
	 * @param angle to be normalized 0 to 360.
	 * @return a value between 0 to 360.
	 */
	public double normalizeAngle( double angle ) {
		// return ((angle %  360) + 360) % 360;
		return angle % 360;
	}

	/** @return a getAngle() value normalized between -180 to 180. */
	public double getNormalizedTo180() {
		return normalizeTo180(this.getAngle());
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
}