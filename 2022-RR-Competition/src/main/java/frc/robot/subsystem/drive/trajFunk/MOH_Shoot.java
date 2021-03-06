package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.Shooter;
import frc.robot.subsystem.drive.Drive;
import frc.util.Bitwise;
import frc.util.PIDXController;

/**
 * This ATrajFunction holds heading while it moves distance.
 * Then shoots while moving or while drifting.  But only allowed to
 * drift for 2 seconds.
 */
public class MOH_Shoot extends ATrajFunction {

    // General
    private double hdgSP = 0.0;         //Hdg to follow.
    private double distDrvSP = 0.0;     //Disance to Move on Hdg
    private double distShtSP = 0.0;     //Distance before it shoots, can be more than drive if drifting
    private double pwrMx = 0.0;         //Max power to apply to pidDist. 
    private int errHdgLT = 180;         //Hdg DB when doing just turn to hdg, case 1.
    private boolean hasShot = false;    //Have requested Shot by Shooter
    private int opts = 3;               //Opts: bit0(1)-shootL, bit1(2)-shootR, bit3(4)-shoot drift always

    // dont use negative power - why?

    /**
     * Constructor
     * @param eHdg - Hdg to follow.
     * @param eDistDrv - Disance to Move on Hdg
     * @param eDistSht - Distance before it shoots, can be more than drive if drifting
     * @param ePwr - Max power to apply to pidDist.
     * @param errHdgLT - Hdg DB when doing just turn to hdg, case 1.
     * @param eOpts - bit0(1)-shootL, bit1(2)-shootR, bit3(4)-shoot drift always
     */
    public MOH_Shoot(double eHdg, double eDistDrv, double eDistSht, double ePwr, int eErrHdgLT, int eOpts) {
        hdgSP = eHdg;
        distDrvSP = eDistDrv;
        distShtSP = eDistSht;
        pwrMx = Math.abs(ePwr);
        errHdgLT = Math.abs(eErrHdgLT);
        opts = eOpts;
    }

    /**
     * Constructor
     * @param eHdg - Hdg to follow.
     * @param eDistDrv - Disance to Move on Hdg
     * @param eDistSht - Distance before it shoots, can be more than drive if drifting
     * @param ePwr - Max power to apply to pidDist.
     * @param errHdgLT - Hdg DB when doing just turn to hdg, case 1.
     */
    public MOH_Shoot(double eHdg, double eDistDrv, double eDistSht, double ePwr, int eErrHdgLT) {
        hdgSP = eHdg;
        distDrvSP = eDistDrv;
        distShtSP = eDistSht;
        pwrMx = Math.abs(ePwr);
        errHdgLT = Math.abs(eErrHdgLT);
    }

    /**
     * Constructor - errHdgLT defaults to 180
     * @param eHdg - Hdg to follow.
     * @param eDistDrv - Disance to Move on Hdg
     * @param eDistSht - Distance before it shoots, can be more than drive if drifting
     * @param ePwr - Max power to apply to pidDist.
     */
    public MOH_Shoot(double eHdg, double eDistDrv, double eDistSht, double ePwr) {
        this(eHdg, eDistDrv, eDistSht, ePwr, 180);
    }

    /**
     * Constructor - ePwr defaults to 1.0 & errHdgLT to 180
     * @param eHdg - Hdg to follow.
     * @param eDistDrv - Disance to Move on Hdg
     * @param eDistSht - Distance before it shoots, can be more than drive if drifting
     */
    public MOH_Shoot(double eHdg, double eDistDrv, double eDistSht) {
        this(eHdg, eDistDrv, eDistSht, 1.0, 180);
    }

    public void execute() {
        // distFB += 0.5;
        switch (state) {
        case 0: // Init Trajectory, turn to hdg then (1) ...
            //Set extended values pidRef,    SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidHdg, hdgSP, (1.0/70), 3.0, 0.3, pwrMx, 1.0, true);
            //Set extended values  pidRef,     SP,          PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidDist, distDrvSP, (-1.0/7), 0.3, 0.3, pwrMx, 1.0, true);

            sqOrQT = false; //tank(1)/arcade(2)-apply sqrt | curvature(3)-quick turn
            diffType = 2;   //0-Off | 1=tank | 2=arcade | 3=curvature

            Drive.distRst();
            initSDB();
            state++;
            trajTmr.clearTimer();
            System.out.println("MOHS - 0");
        case 1: // IF position error (hdg) GT errHdgLT turn first the MOH
            System.out.println("MOHS - 1");
            trajCmd[0] = pidHdg.calculateX(hdgFB());   //cmd[0]=rotate(X), [1]=fwd(Y)
            trajCmd[1] = 0.0;
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2);    //Send to Drive system
            // prtShtuff("MOHS");
            if(chkHasShot(hasShot, distShtSP, opts)) hasShot = true;              // Chk to shoot
            if (Math.abs(pidHdg.getPositionError()) < errHdgLT ) state++;   // Chk hdg error
            break;
        case 2: // Move forward, steer Auto Heading and Dist
            System.out.println("MOHS - 2: " + distFB());
            trajCmd[0] = pidHdg.calculateX(hdgFB());   //cmd[0]=rotate(X), [1]=fwd(Y)
            trajCmd[1] = pidDist.calculateX(distFB()); //cmd[0]=rotate(X), [1]=fwd(Y)
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2);
            // prtShtuff("MOH2");
            if(chkHasShot(hasShot, distShtSP, opts)) hasShot = true;          // Chk to shoot
            if (pidDist.atSetpoint() && pidHdg.atSetpoint()) state++;   // Chk both done
            break;
        case 3: // Allow bot to drift
            sendDriveCmds(0.0, 0.0, false, 2);      // drifting
            if(trajTmr.hasExpired(3.0, state)) {    // but only for up to 2 seconds
                if(Bitwise.isBitSet(opts, 2)) chkHasShot(hasShot, 0.0, opts);  // if optioned, shoot anyway
                hasShot = true;
                state++;
            }else if(chkHasShot(hasShot, distShtSP, opts)){     // or shoot before 2 seconds
                hasShot = true;
                state++;
            }
            System.out.println("MOHS - 3: ---------- Drifting: " + trajTmr.hasExpired());
            break;
        case 4: // Done
            setDone();  //Flag done and stop motors
            System.out.println("MOHS - 4: ---------- Done -----------");
            break;
        default:
            setDone();  //Flag done and stop motors
            System.out.println("MOHS - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
        // return strCmd;
    }

    private static boolean chkHasShot(boolean _hasShot, double _distShtSP, int opts){
        // if(!_hasShot && (distFB() > _distShtSP)){
        double err = Math.signum(_distShtSP) * (_distShtSP - distFB());
        System.out.println("chkHasShot: " + " \thasShot: " + _hasShot + " \terr: " + err + " \topts: "+ opts + " \tbit0: " + Bitwise.isBitSet(opts, 0));
        if(!_hasShot && (err <= 0.0)) {
            // Shooter.reqLowDA_L = false;
            // Shooter.reqLowDA_R = false;
            Shooter.reqLowDA_L = (Bitwise.isBitSet(opts, 0)) ? false : null; //If optioned, shoot left
            Shooter.reqLowDA_R = (Bitwise.isBitSet(opts, 1)) ? false : null; //If optioned, shoot right
            return true;
        }
        return _hasShot;
    }
}
