package frc.robot.subsystem.drive;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.util.PIDXController;

/**
 * This is the super class for Drv_Auto & Drv_Teleop.
 * <p>Handles common variables & methods.
 * <p>All commands to drive motors should be issue thru here.
 */
public class Drive {

    // Assignments used by DiffDrv. Slaves sent same command.  Slaves set to follow Masters in IO.
    private static DifferentialDrive diffDrv = IO.diffDrv_M;

    private static double lSpdY, rSpdRot_XY;    //Cmd values
    private static boolean isSqOrQT;            //DiffDrvshould sq input or quick turn
    private static int diffType;                //0-Off | 1=tank | 2=arcade | 3=curvature

    private static boolean swapFront;       // front of robot is swapped
    private static boolean scaleOutput;     // scale the output signal
    private static double scale = 0.5;       //Scale to apply to output is active
    private static Double hdgHold = null;   // Hold heading, if not null
    public static PIDXController pidHdgHold = new PIDXController(); //PIDX for hdgHold


    // public static double deadband = 0.45;

    // /*                [0][]=hdg [1][]=dist SP, PB, DB, Mn, Mx, Xcl */
    // private static double[][] parms = { { 0.0, -110.0, 1.0, 0.55, 1.0, 0.20 },
    // /*                               */ { 0.0, 10.0, 0.7, 0.45, 1.0, 0.07 } };
    // public static Steer steer = new Steer(parms);  //Create steer instance for hdg & dist, use default parms

    public static PIDXController pidHdg, pidDist;
    public static double strCmd[] = new double[2]; //Storage for steer return

    public static double hdgFB() {return IO.navX.getNormalizedTo180();}  //Only need hdg to Hold Angle 0 or 180
    public static void hdgRst() { IO.navX.reset(); }
    public static double distFB() { return IO.coorXY.drvFeet(); }
    public static void distRst() { IO.coorXY.drvFeetRst(); }

    public static void init() {
        cmdUpdate(0.0, 0.0, false, 0);
        pidHdgHold = new PIDXController(1.0/45, 0.0, 0.0);
        pidHdgHold.enableContinuousInput(-180.0, 180.0);
        pidHdgHold.setInDB(2.0);
        pidHdgHold.setOutMn(0.30);
        pidHdgHold.setOutMx(1.0);
        pidHdgHold.setOutExp(1.0);
    }

    /**
     * Determine any state that needs to interupt the present state, usually by way of a JS button but
     * can be caused by other events.
     */
    public static void update() {
        smUpdate();
        sdbUpdate();
    }

    /**
     * Called from Robot telopPerodic every 20mS to Update the drive sub system.
     */
    private static void smUpdate() {
    }

    private static void sdbUpdate() {
    }

    /**
     * Common interface for all diff drv types
     * 
     * @param lSpdY - tank(1)-left JS | arcade(2)-fwd  |  curvature(3)-fwd 
     * @param rSpdRot_XY - tank(1)-right JS | arcade(2)-rotation  |  curvature(3)-rotation
     * @param isSqOrQT - tank(1)/arcade(2)-apply sqrt  |  curvature(3)-quick turn
     * @param diffType - 0-Off  |  1=tank  |  2=arcade  |  3=curvature
     */
    public static void cmdUpdate(double _lSpdY, double _rSpdRot_XY, boolean _isSqOrQT, int _diffType) {
        lSpdY = _lSpdY;  rSpdRot_XY = _rSpdRot_XY; isSqOrQT = _isSqOrQT; diffType = _diffType;
        chkInput();     //Chk for hdg hold, front swap or scaling
        switch(diffType){
            case 0:     //Off
            diffDrv.tankDrive(0.0, 0.0, false);
            break;
            case 1:     //Tank
         //   diffDrv.tankDrive(JS_IO.axLeftY.get(), -JS_IO.axRightY.get(), isSqOrQT);
            diffDrv.tankDrive(-lSpdY, -rSpdRot_XY, isSqOrQT);
            break;
            case 2:     //Arcade
            diffDrv.arcadeDrive(-lSpdY, rSpdRot_XY, isSqOrQT);
            break;
            case 3:     //Curvature
            diffDrv.curvatureDrive(-lSpdY, rSpdRot_XY, isSqOrQT);
            break;
            default:
            diffDrv.tankDrive(0.0, 0.0, false);
            System.out.println("Bad Diff Drive type - " + diffType);
        }
        // System.out.println("Drive-" + diffType + ":\tlSpd: " + lSpdY + "\trSpdRot: " + rSpdRot_XY);
        SmartDashboard.putNumber("Drive/MtrL Out", IO.drvLead_L.get());
        SmartDashboard.putNumber("Drive/MtrR Out", IO.drvLead_R.get());
    }

    /**
     * Defaults to no squaring and arcade drive
     * @param [0] = rotation, [1] = fwd
     */
    public void cmdUpdate(double tCmd[]){
        cmdUpdate(tCmd[0], tCmd[1], false, 2);
    }

    /**
     * Defaults to no squaring
     * @param [0] = rotation, [1] = fwd
     * @param dType - 0-Off  |  1=tank  |  2=arcade  |  3=curvature
     */
    public void cmdUpdate(double tCmd[], int dType){
        cmdUpdate(tCmd[0], tCmd[1], false, dType);
    }

    /**Chk for angle hold, front swap or scaling */
    private static void chkInput(){
        if(diffType > 0 && diffType < 4){   //If tank(1), arcade(2) or curve(3)
            chkHdgHold();
            chkSwapFront();
            chkScale();
        }
    }

    /**Condition JS input for Hold angle. */
    private static void chkHdgHold() {
        if(hdgHold != null){                //If call for hold angle
            strCmd[0] = pidHdgHold.calculateX(hdgFB(), hdgHold);             //Calc rotation
            rSpdRot_XY = swapFront ? -strCmd[0] : strCmd[0];  //store in rot, neg if front swap
            if(diffType == 1) diffType = 2;                 //If type tank Chg to arcade
        }
    }

    /**Condition JS input for front swap. */
    private static void chkSwapFront() {
        if(swapFront){
            lSpdY *= -1.0;  rSpdRot_XY *= -1.0; //Negate values
            if(diffType == 1){                  //If tank swap left and right also
                strCmd[0] = lSpdY;              //use strCmd as tmp storage to swap sides
                lSpdY = rSpdRot_XY;
                rSpdRot_XY = strCmd[0];
            }
        }
    }

    /**Condition JS input for scaling.
     * Set the max output of the diffDrv */
    private static void chkScale(){ 
        diffDrv.setMaxOutput(getWkgScale());
    }

    /**Set if front and backof robot should be swapped. */
    public static void setSwapFront(boolean _swapFront){ swapFront = _swapFront; }

    /**@return true if front and backof robot are swapped. */
    public static boolean isSwappedFront(){ return swapFront; }

    /**Set and hold robot on a heading.
     * <p>Can pass null or use () to release
     * <p>Or call relHdgHold();
     * @param hdg to hold (else null)
     */
    public static void setHdgHold(Double hdg){ hdgHold = hdg; }
    public static void setHdgHold(){ relHdgHold(); }

    /**Release angle hold */
    public static void relHdgHold(){ hdgHold = null; }

    /**@return true if robot is being held on a hdg. */
    public static boolean isHdgHold(){ return hdgHold != null; }

    /**@return the heading being held else return null */
    public static Double getHdgHold(){ return hdgHold; }

    /**@param _scaleOutput to scale the output max to scale else use 1.0 */
    public static void setScaled(boolean _scaleOutput){ scaleOutput = _scaleOutput; }

    /**@param _scaleOutput to scale the output max to scale else use 1.0 */
    public static boolean isScaled(){ return scaleOutput; }

    /**@param _scale value to use if scaleOutput is set */
    public static void setScaledOut(double _scale){ scale = _scale; }

    /**@return value of scale used if scaleOutput is set */
    public static double getScaledOut(){ return scale; }

    /**@return scale if scaleOutput is set else return 1.0 */
    public static double getWkgScale(){ return scaleOutput ? scale : 1.0; }

    //------------------- Legacy -------------------------
    /**
     * Stop moving  (Legacy & quick calls)
     */
    public static void cmdUpdate() { cmdUpdate(0.0, 0.0, true, 0); }

    /**
     * Tank drive
     * @param lSpdY - left tank control
     * @param rSpdRot_XY - right tank control
     */
    public static void cmdUpdate(double lSpdY, double rSpdRot_XY) { cmdUpdate(lSpdY, rSpdRot_XY, false, 1); }

    /**
     * Tank or Arcade drive  (Legacy & quick calls, Sqr true.)
     * @param lSpdY - tank-left | arcade-fwd
     * @param rSpdRot_XY - tank-right | arcade-rotation
     * @param isTank else arcade
     */
    public static void cmdUpdate(double lSpdY, double rSpdRot_XY, boolean isTank) {
        cmdUpdate(lSpdY, rSpdRot_XY, true, isTank ? 1 : 2);
    }
}
