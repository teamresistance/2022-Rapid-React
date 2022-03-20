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
            runTm = System.currentTimeMillis();
            // pidHdg = new PIDXController(1.0/70, 0.0, 0.0);
            // pidHdg.enableContinuousInput(-180.0, 180.0);
            //Set extended values          SP,        PB,  DB,   Mn,  Mx,   Exp,   Clmp
            PIDXController.setExt(pidHdg, hdgSP, (1.0/60), 5.0, 0.35, pwrMx, 2.0, true);
            // pidHdg.setP(1.0/70);z

            // pidDist = new PIDXController(-1.0/10, 0.0, 0.0);
            //Set extended values            SP,        PB,  DB,   Mn,  Mx,   Exp,   Clmp
            PIDXController.setExt(pidDist, distSP, (-1.0/3), 1.0, 0.2, pwrMx, 1.0, true);
            // pidDist.setP(-1.0/10);

            Drive.distRst();
            initSDB();
            // sendDriveCmds(0.0, 0.0, false, 2);
            state++;
            // System.out.println("TNM - 0  -----  PB:" + pidHdg.getP());
            System.out.println("TNM 0, runtm: " + ((System.currentTimeMillis() - runTm) / 1000.0));
            // break;
        case 1: // Turn to heading.  Do not move forward, yet.
            runTm = System.currentTimeMillis();
            // System.out.println("TNM - 1: Pre");
            trajCmd[0] = pidHdg.calculateX(hdgFB());
            sendDriveCmds(0.0, trajCmd[0], false, 2);
            System.out.println("TNM - 1: Post DrvCmdXY:  " + trajCmd[0]);
            // Chk if hdg is done
            if (pidHdg.atSetpoint()) {
                state++;    // Chk hdg only
                Drive.distRst();
            }
            // prtShtuff("TNM");
            System.out.println("TNM 1, runtm: " + ((System.currentTimeMillis() - runTm) / 1000.0));
            break;
        case 2: // Move forward, steer Auto Heading and Dist
            // runTm = System.currentTimeMillis();
            trajCmd[0] = pidHdg.calculateX(hdgFB());
            trajCmd[1] = pidDist.calculateX(distFB());
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2);
            // Chk if distance is done
            if (pidDist.atSetpoint()) state++; // Chk distance only
            // System.out.println("TNM - 2  -----  hdgFB: " + hdgFB() +" " + IO.navX.getNormalizedTo180());
            System.out.println("TNM - 2  -----  distFB: " + distFB() + "cmd: " + trajCmd[1]);
            // prtShtuff("TNM");
            // System.out.println("TNM 2, runtm: " + ((System.currentTimeMillis() - runTm) ));
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