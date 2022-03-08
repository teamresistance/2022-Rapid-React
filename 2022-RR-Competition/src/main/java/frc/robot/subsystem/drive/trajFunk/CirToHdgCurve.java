package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This ATrajFunction uses Curve drive to circle a radius distance.
 * It calculates the radius to control the rotation(JSX) command from the
 * present XY position in IO to the center XY passed.  The forward(JSY) command
 * is calculated until heading is reached.
 */
public class CirToHdgCurve extends ATrajFunction {

    private double ctrX;
    private double ctrY;
    private double radiusSP = 0.0;
    private double fwdCmd = 0.0;
    private double rotCmd = 0.0;
    private double hdgSP = 0.0;
    // private boolean turnRight;

    /**
     * Constructor
     * @param _ctrX, X coordinate of circle
     * @param _ctrY, Y coordinate of circle
     * @param _radiusSP, radius to maintain - positve
     * @param _hdgSP, heading to complete circle
     * @param _fwdCmdBase, forward startup command - positive
     * @param _rotCmdBase, rotation startup command
     */
    public CirToHdgCurve(double _ctrX, double _ctrY, double _radiusSP,
                         double _hdgSP, double _fwdCmdBase, double _rotCmdBase) {
        ctrX = _ctrX;
        ctrY = _ctrY;
        radiusSP = Math.abs(_radiusSP);
        hdgSP = _hdgSP;
        fwdCmd = Math.abs(_fwdCmdBase);
        rotCmd = _rotCmdBase;
        // turnRight = Math.abs(rotCmd) > 0.0;
    }

    /*Trying something different.
    Using Steer hdg to adjust passed, base, curvature speed, 
    gets closer slows down a little and check for done hdg.
    This is appliedto fwd cmd[1].
    Also use Steer dist to adjust passed, base, rotation
    to maintain radius.  Rot passed "should" be < 0.9 to 
    allow adjust to tighter curve if needed.
    This is applied to rot cmd[0].
    Both are inverted from what we normally get from steerTo.
    */
    public void execute() {
        switch (state) {
        case 0: // Init traj
            // pidHdg = new PIDXController(-1.0/45, 0.0, 0.0);
            pidHdg.enableContinuousInput(-180.0, 180.0);
            //Set extended values SP, DB, Mn, Mx, Exp, Cmp
            PIDXController.setExt(pidHdg, hdgSP, 2.0, 0.35, fwdCmd, 2.0, true);

            // pidDist = new PIDXController(1.0/2, 0.0, 0.0);
            //Set extended values SP, DB, Mn, Mx, Exp, Cmp
            PIDXController.setExt(pidDist, radiusSP, 0.3, (0.6 * fwdCmd), fwdCmd, 1.0, false);
            //P must be the inverse of the sign of rotCmd.
            pidDist.setP(-Math.copySign(pidDist.getP(), rotCmd));
            pidDist.setOutFF(rotCmd);
            pidDist.setIntegratorRange(-0.5, 0.5);

            System.out.println("CHC - 0  ----------- Start ------------");
            initSDB();
            state++;
        case 1: // Rotate forward, steer with Dist to maintain radius
            trajCmd[0] = pidHdg.calculateX(hdgFB);                   //[0]=fwd(Y)
            trajCmd[1] = pidDist.calculateX(radiusFB(ctrX, ctrY));   //[1]=radius(X)
            // strCmd[1] = pidDist.calculateX(radiusFB);   //[1]=radius(X)
            prtShtuff("CHC");    //--------- Print info --------------
            // if(turnRight){

            // }else{

            // }
            Drive.cmdUpdate(trajCmd[0], trajCmd[1], false, 3); // cmdUpdate for curvature hdg & dist.
            if (pidHdg.atSetpoint()) state++;    // Chk hdg only
            break;
        case 2:
            setDone();
            System.out.println("CHC - 2: Final Rotate Cmd: " + trajCmd[1]);
            break;
        default:
            setDone();
            System.out.println("CHC - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
    }
}
