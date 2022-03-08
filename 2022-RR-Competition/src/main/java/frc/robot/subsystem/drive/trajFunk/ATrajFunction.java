/*
Desc.  This is a super class for control of autonomous driving methods and
other autonomous subsystems, ie. Snorfler.

History:
3/1/2020 - Kinfe Bankole - Initial release
7/11    - JCH - Chgd to PIDX from TSteer and some cleanup
*/

package frc.robot.subsystem.drive.trajFunk;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This is a collection of classes to control driving (and robot)
 * during Autonomous.
 */
public abstract class ATrajFunction {

    public static int state = 0;
    private static boolean done = false;
    public static PIDXController pidHdg = Drive.pidHdg;
    public static PIDXController pidDist = Drive.pidDist;

    public static double hdgFB() {return IO.navX.getNormalizedTo180();}  //Only need hdg to Hold Angle 0 or 180
    public static double distFB() {return IO.coorXY.drvFeet();}  //Only need hdg to Hold Angle 0 or 180
    public static double[] trajCmd = new double[2];
    public static boolean sqOrQT = false;
    public static int diffType = 0;

    public static double hdgFB = 0;     //For simulator testing
    public static double distFB = 0;
    public static double radiusFB = 0;


    public static void initTraj() {
        state = 0;
        done = false;
        System.out.println("ATraj - Init");
    }

    /**Execute this trajectory */
    public void execute() {

    }

    public static void setDone() {
        done = true;
        Drive.cmdUpdate();      //Stop motors, Neede to keep diffDrv active
    }

    public static boolean getDone() {
        return done;
    }

    public static double[] getTrajCmd(){ return trajCmd; }

    public static boolean getSqOrQT(){ return sqOrQT; }

    public static int getDiffType(){ return diffType; }

    //------------------------------ Helper methods ------------------------

    // /**In a PIDXController set extended values for
    //  * <p> SP, DB, Mn, Mx, Exp, Cmp
    //  */
    // public static void setExt(PIDXController aPidX, double sp, double db, 
    // /*                      */double mn, double mx,double exp, Boolean clmp){
    //     aPidX.setSetpoint(sp);
    //     aPidX.setInDB(db);
    //     aPidX.setOutMn(mn);
    //     aPidX.setOutMx(mx);
    //     aPidX.setOutExp(exp);
    //     aPidX.setClamp(clmp);
    // }

    /**
     * Calculate the heading & distance from existing coor to coor passed.
     * @param _wpX X coor of the waypoint
     * @param _wpY Y coor of the waypoint
     * @return double[2] with the heading[0](JSX) & distance[1](JSY) from robot to waypoint.
     * */
    public static double[] wpCalcHdgDistSP(double _wpX, double _wpY){
        double deltaX = _wpX - IO.coorXY.getX();    //Adjacent
        double deltaY = _wpY - IO.coorXY.getY();    //Opposite
        double[] tmp = new double[2];   //[0] is hdgSP, [1] is distSP
        
        tmp[1] = Math.hypot(deltaX, deltaY);

        if( deltaY == 0){
            tmp[0] = deltaX < 0 ? -90 : 90;
        }else{
            tmp[0] = Math.toDegrees(Math.atan(deltaX/deltaY));
            if(deltaY < 0){
                tmp[0] += deltaX < 0 ? -180 : 180; 
            }
        }
        return tmp;
    }

    /**
     * Calculate the heading from existing coor to coor passed.
     * @param _wpX X coor of the waypoint
     * @param _wpY Y coor of the waypoint
     * @return the heading from robot to waypoint.
     * */
    public static double wpCalcHdgSP(double _wpX, double _wpY){
        double deltaX = _wpX - IO.coorXY.getX();    //Adjacent
        double deltaY = _wpY - IO.coorXY.getY();    //Opposite
        double _hdgSP;
        if( deltaY == 0){
            _hdgSP = deltaX < 0 ? -90 : 90;
        }else{
            _hdgSP = Math.toDegrees(Math.atan(deltaX/deltaY));
            if(deltaY < 0){
                _hdgSP += deltaX < 0 ? -180 : 180; 
            }
        }
        return _hdgSP;
    }

    /**
     * Calculate the distance from existing coor to coor passed.
     * @param _wpX X coor of the waypoint
     * @param _wpY Y coor of the waypoint
     * @return the distance from robot to waypoint.
     * */
    public static double wpCalcDistSP(double _wpX, double _wpY){
        return Math.hypot(_wpX - IO.coorXY.getX(),    //Adjacent
        /*              */_wpY - IO.coorXY.getY());   //Opposite
    }

    /**
     * Calculate the distance from existing coor to coor passed.
     * @param _ctrX X coor of center of the circle
     * @param _ctrY Y coor of center of the circle
     * @return the distance from robot to center.
     */
    public static double radiusFB(double _ctrX, double _ctrY){
        return Math.hypot(_ctrX - IO.coorXY.getX(),    //Adjacent
        /*              */_ctrY - IO.coorXY.getY());   //Opposite
    }

    /**Trajectory SDB initialize */
    public static void initSDB() {
        PIDXController.initSDBPid(pidHdg, "Auto/pidHdg");
        PIDXController.initSDBPid(pidDist, "Auto/pidDist");

        SmartDashboard.putNumber("Drv/Auto/pidTst/1_HdgOut", pidHdg.getOut());
        SmartDashboard.putNumber("Drv/Auto/pidTst/2_HdgAdj", pidHdg.getAdj());
        SmartDashboard.putBoolean("Drv/Auto/pidTst/3_atSP", pidHdg.atSetpoint());

        SmartDashboard.putNumber("Drv/Auto/pidTst/A_DistOut", pidDist.getOut());
        SmartDashboard.putNumber("Drv/Auto/pidTst/B_DistAdj", pidDist.getAdj());
        SmartDashboard.putBoolean("Drv/Auto/pidTst/C_atSP", pidDist.atSetpoint());
    }

    /**Trajectory SDB update */
    public static void updSDB() {
        PIDXController.updSDBPid(pidHdg, "Auto/pidHdg");
        PIDXController.updSDBPid(pidDist, "Auto/pidDist");

        SmartDashboard.putNumber( "Drv/Auto/pidTst/1_HdgOut", pidHdg.getOut());
        SmartDashboard.putNumber( "Drv/Auto/pidTst/2_HdgAdj", pidHdg.getAdj());
        SmartDashboard.putBoolean("Drv/Auto/pidTst/3_atSP", pidHdg.atSetpoint());

        SmartDashboard.putNumber( "Drv/Auto/pidTst/A_DistOut", pidDist.getOut());
        SmartDashboard.putNumber( "Drv/Auto/pidTst/B_DistAdj", pidDist.getAdj());
        SmartDashboard.putBoolean("Drv/Auto/pidTst/C_atSP", pidDist.atSetpoint());

        SmartDashboard.putNumber("Drv/Auto/pidTst/M_CoorX", IO.coorXY.getX());
        SmartDashboard.putNumber("Drv/Auto/pidTst/M_CoorY", IO.coorXY.getY());
    }

    /**Print common stuff for pidHdg, pidDist & coors XY.  Pid SP, FB & cmd
     * @param traj name (tag) to ID the print as "tag - state:"
     */
    public static void prtShtuff(String traj){
        System.out.println(traj + " - " + state + ": Feet\t" + IO.coorXY.drvFeet());
        System.out.println("\t\tdist   SP: " + pidDist.getSetpoint() + "\tFB: " + pidDist.getInFB() + "\tcmd: " + pidDist.getAdj());
        System.out.println("\t\thdg\tSP: " + pidHdg.getSetpoint() + "\tFB: " + pidHdg.getInFB() + "\tcmd: " + pidHdg.getAdj());
        System.out.println("\tCoor\tX: " + IO.coorXY.getX() + "\tY " + IO.coorXY.getY() + "\tHdg " + pidHdg.getInFB());
    }

}
