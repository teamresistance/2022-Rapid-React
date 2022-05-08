package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This ATrajFunction calculates a heading and distance from the 
 * present XY position in IO and the Waypoint XY passed.  It then
 * steers on the calculated hdg & distance.
 * <p>This version hdg & dist are continuously recalculated.
 * <p>Doesn't seem to work well may take another look.
 */
public class Waypt3 extends ATrajFunction {

    private double wpX = 0.0;
    private double wpY = 0.0;
    private double pwrMx = 1.0;

    /**
     * Constructor for turn to hdg before moving on hdg.
     * @param _wpX target X coordinate
     * @param _wpY target Y coordinate
     * @param _pwrMx max power to use
     */
    public Waypt3(double _wpX, double _wpY, double _pwrMx) {
        wpX = _wpX;    wpY = _wpY;
        pwrMx = Math.abs(_pwrMx);   // dont use negative power
    }

    /**Constructor for immediately move on hdg and use 1.0 max power */
    public Waypt3(double _wpX, double _wpY) {
        this(_wpX, _wpY, 1.0);
    }

    public void execute() {
        // update();
        switch (state) {
        case 0: // Init Trajectory
            // pidHdg = new PIDXController(1.0/50, 0.0, 0.0);
            // pidHdg.enableContinuousInput(-180.0, 180.0);
            //Set extended values pidRef,  SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidHdg, 0.0, (1.0/50), 2.0, 0.4, pwrMx, 2.0, true);

            // pidDist = new PIDXController(-1.0/8, 0.0, 0.0);
            //Set extended values  pidRef,  SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidDist, 0.0, (-1.0/8), 0.3, 0.3, pwrMx, 1.0, true);

            trajCmd = wpCalcHdgDistSP(wpX, wpY); //Get present XY Loc and calc hdg & distSP's (static)
            pidHdg.setSetpoint(trajCmd[0]);  //Used strCmd as tmp holder
            pidDist.setSetpoint(trajCmd[1]);

            Drive.distRst();
            initSDB();
            System.out.println("WPT3 - 0 hdgSP: " + pidHdg.getSetpoint() + "\tdistSP: " + pidDist.getSetpoint());
            state++;
        case 1: // Move forward, steer Auto Heading and Dist
            trajCmd = wpCalcHdgDistSP(wpX, wpY); //Get present XY Loc and calc hdg & distSP's (static)
            pidHdg.setSetpoint(trajCmd[0]);  //Used strCmd as tmp holder
            pidDist.setSetpoint(trajCmd[1]);

            trajCmd[0] = pidHdg.calculateX(hdgFB()); //cmd[0]=rotate(JSX)
            trajCmd[1] = pidDist.calculateX(distFB()); //cmd[1]=fwd(JSY)
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2); // cmdUpdate for hdg & dist.
            prtShtuff("WPT3");
            if (pidDist.atSetpoint() && pidHdg.atSetpoint()) state++; //Chk hdg & dist done.
            break;
        case 2: //Done
            setDone();
            System.out.println("WPT3 - 2: ---------- Done -----------");
            break;
        default:
            setDone();
            System.out.println("WPT3 - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
    }

}
