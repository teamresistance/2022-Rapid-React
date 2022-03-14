package frc.robot.subsystem.driveSave.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This ATrajFunction uses Tank drive to circle a radius distance.
 * It calculates the radius to control the inside(JSX) wheel command from the
 * present XY position in IO to the center XY passed.  The outside(JSY) command
 * is calculated until heading is reached.
 * NOTE: May need to control the outside and hold the inside.  Testing.
 */
public class CirToHdgTank extends ATrajFunction {

    private double ctrX;
    private double ctrY;
    private double radiusSP;
    private double hdgSP;
    private double lCmd;
    private double rCmd;
    private boolean turnRight;

    /**
     * Constructor to make a circle using tank steering.
     * @param _ctrX
     * @param _ctrY
     * @param _radiusSP
     * @param _hdgSP
     * @param _lCmdBase
     * @param _rCmdBase
     */
    public CirToHdgTank(double _ctrX, double _ctrY, double _radiusSP,
                         double _hdgSP, double _lCmdBase, double _rCmdBase) {
        ctrX = _ctrX;
        ctrY = _ctrY;
        radiusSP = _radiusSP;
        hdgSP = _hdgSP;
        lCmd = _lCmdBase;
        rCmd = _rCmdBase;
        turnRight = Math.abs(lCmd) > Math.abs(rCmd);

        radiusFB_sim = radiusSP;
    }

    /*Trying something different.
    Using Steer hdg to adjust the higher speed wheel.Prop only, 
    gets closer slows down a little and check for done hdg.
    cmd[1] is applied to the outside wheel.
    Using Steer dist to adjust the lower spped wheel.  Integration only,
    to maintain radius.  Rot passed "should" be < 0.9 to 
    allow adjust to tighter curve if needed.
    cmd[0] is applied to the inside wheel.
    Both are inverted from what we normally get from steerTo.
    */
    public void execute() {
        switch (state) {
        /*Initialize objects here.  pidHdg/Dist are shared with
        other ATrajFunctions, as others.  This is executed while
        this traj is active.
        */
        case 0: // Init Trajectory
            // pidHdg = new PIDXController(-1.0/45, 0.0, 0.0);
            // pidHdg.enableContinuousInput(-180.0, 180.0);
            //Set extended values pidCtlr, SP, DB, Mn, Mx, Exp, Cmp
            PIDXController.setExt(pidHdg, hdgSP, 5.0, 0.5, 1.0, 2.0, true);
            pidHdg.setOutMx(turnRight ? lCmd : rCmd);   //Outside whl
            pidHdg.setOutMn(0.98 * pidHdg.getOutMx());

            // pidDist = new PIDXController(1.0/0.4, 0.0, 0.0);
            //Set extended values pidCtlr, SP, DB, Mn, Mx, Exp, Cmp
            PIDXController.setExt(pidDist, radiusSP, 0.0, 0.0, 0.5, 2.0, true);
            pidDist.setIntegratorRange(-0.5, 0.5);  //Limt integration to +/-
            pidDist.setOutFF(turnRight ? rCmd : lCmd);  //Inside whl
            
            initSDB();
            state++;
            System.out.println("CHT - 0 \thdgSP: " + pidHdg.getSetpoint() + "\tdistSP: " + pidDist.getSetpoint());
        case 1: // Turn to heading.  Mx spd on out whl.  Hold radius on inside whl.
            trajCmd[0] = pidHdg.calculateX(hdgFB());              //cmd[0]=fwd(Outside)
            trajCmd[1] = pidDist.calculateX(radiusFB(ctrX, ctrY));//cmd[1]=radius(Inside)
            trajCmd[0] += trajCmd[1];
            trajCmd[1] = 0.0;
            // strCmd[1] = pidDist.calculateX(radiusFB);//cmd[1]=radius(Inside)
            System.out.println("rad cmd0: " + trajCmd[0] + "\tcmd1: " + trajCmd[1] );
            if(turnRight){      //If left is inside wheel
                Drive.cmdUpdate(trajCmd[0], trajCmd[1], false, 1); //Turning right, left whl is outside
            }else{              //else right is inside wheel
                Drive.cmdUpdate(trajCmd[1], trajCmd[0], false, 1); //Turning left, right whl is outside
            }
            if (pidHdg.atSetpoint()) state++;    // Chk hdg only
            prtShtuff("CHT");
            break;
        case 2: // Done
            setDone();
            System.out.println("CHT - 2: Final Inside Cmd: " + trajCmd[turnRight ? 1 : 0]);
            break;
        default:
            setDone();
            System.out.println("CHT - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
    }
}
