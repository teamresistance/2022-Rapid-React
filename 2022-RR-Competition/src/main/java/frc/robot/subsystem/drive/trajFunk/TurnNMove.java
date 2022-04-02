package frc.robot.subsystem.drive.trajFunk;

import frc.io.hdw_io.IO;
import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This AutoFunction uses Arcade to turn to heading THEN moves distance.
 */
public class TurnNMove extends ATrajFunction {

    // private boolean finished = false;
    private double hdgSP = 0.0;
    private double distSP = 0.0;
    private double pwrMx = 0.0;
    private long runTm = 0L;

    
    public TurnNMove(double eHdg, double eDist, double ePwr) {
        hdgSP = eHdg;
        distSP = eDist;
        pwrMx = Math.abs(ePwr); // dont use negative power
    }

    public TurnNMove(double eHdg, double eDist) {
        hdgSP = eHdg;
        distSP = eDist;
        pwrMx = 1.0;
    }

    public void execute() {
        // update();
        switch (state) {
        case 0: // Init Trajectory, (1)turn to hdg then (2)moveto dist ...
            // pidHdg = new PIDXController(1.0/70, 0.0, 0.0);
            // pidHdg.enableContinuousInput(-180.0, 180.0);
            //Set extended values pidRef,    SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidHdg, hdgSP, (1.0/60), 5.0, 0.35, pwrMx, 2.0, true);
            // pidHdg.setP(1.0/70);
            // pidDist = new PIDXController(-1.0/10, 0.0, 0.0);
            //Set extended values  pidRef,     SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidDist, distSP, (-1.0/3), 0.5, 0.2, pwrMx, 1.0, true);
            // pidDist.setP(-1.0/10);

            Drive.distRst();
            initSDB();
            // sendDriveCmds(0.0, 0.0, false, 2);
            state++;
            // System.out.println("TNM - 0  -----  PB:" + pidHdg.getP());
            System.out.println("TNM 0, Dist: " + Drive.distFB());
            // break;
        case 1: // Turn to heading.  Do not move forward, yet.
            // System.out.println("TNM - 1: Pre");
            trajCmd[0] = pidHdg.calculateX(hdgFB());
            sendDriveCmds(0.0, trajCmd[0], false, 2);
            // System.out.println("TNM - 1: Post DrvCmdXY:  " + trajCmd[0]);
            // Chk if hdg is done
            if (pidHdg.atSetpoint()) {  //Check hdg only
                state++;    // then do dist & hdg
                Drive.distRst();
            }
            prtShtuff("TNM-1");
            break;
        case 2: // Move forward, steer Auto Heading and Dist
            trajCmd[0] = pidHdg.calculateX(hdgFB());
            trajCmd[1] = pidDist.calculateX(distFB());
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2);
            // Chk if distance is done
            if (pidDist.atSetpoint()) state++; // Chk distance only
             System.out.println("TNM - 2  -----  distCmd: " + trajCmd[1] + "\thdgCmd: " + trajCmd[0]);
            prtShtuff("TNM-2");
            break;
        case 3:
            // System.out.println("Distance gone in TNM: " + distFB() + " SetPoint: " + pidDist.getSetpoint());
            setDone();
            System.out.println("TNM - 3: ---------- Done -----------");
            break;
        default:
            setDone();
            System.out.println("TNM - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
    }
}