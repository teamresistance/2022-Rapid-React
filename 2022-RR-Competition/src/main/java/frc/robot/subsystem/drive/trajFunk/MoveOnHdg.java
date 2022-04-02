package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This ATrajFunction holds heading while it moves distance.
 */
public class MoveOnHdg extends ATrajFunction {

    // General
    private double hdgSP = 0.0;
    private double distSP = 0.0;
    private double pwrMx = 0.0;
    private int errHdgLT = 180;

    // dont use negative power - why?

    /**
     * Constructor
     * @param eHdg
     * @param eDist
     * @param ePwr
     * @param errHdgLT
     */
    public MoveOnHdg(double eHdg, double eDist, double ePwr, int eErrHdgLT) {
        hdgSP = eHdg;
        distSP = eDist;
        pwrMx = Math.abs(ePwr);
        errHdgLT = Math.abs(eErrHdgLT);
    }

    /**
     * Constructor - errHdgLT defaults to 180
     * @param eHdg
     * @param eDist
     * @param ePwr
     */
    public MoveOnHdg(double eHdg, double eDist, double ePwr) {
        this(eHdg, eDist, ePwr, 180);
    }

    /**
     * Constructor - ePwr defaults to 1.0 & errHdgLT to 180
     * @param eHdg
     * @param eDist
     */
    public MoveOnHdg(double eHdg, double eDist) {
        this(eHdg, eDist, 1.0, 180);
    }

    public void execute() {
        switch (state) {
        case 0: // Init Trajectory, turn to hdg then (1) ...
            // pidHdg = new PIDXController(1.0/45, 0.0, 0.0);
            // pidHdg.enableContinuousInput(-180.0, 180.0);
            //Set extended values SP, PB, DB, Mn, Mx, Exp, Clmp
            //Set extended values pidRef,    SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidHdg, hdgSP, (1.0/70), 5.0, 0.3, pwrMx, 1.0, true);
            // pidHdg.setSetpoint(hdgSP);
            // pidHdg.setInDB(5.0);
            // pidHdg.setOutMn(0.3);
            // pidHdg.setOutMx(pwrMx);
            // pidHdg.setOutExp(1.0);
            // pidDist.setClamp(true);

            // pidDist = new PIDXController(-1.0/10, 0.0, 0.0);
            //Set extended values SP, PB, DB, Mn, Mx, Exp, Clmp
            //Set extended values  pidRef,     SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidDist, distSP, (-1.0/10), 0.5, 0.2, pwrMx, 1.0, true);
            // pidDist.setSetpoint(distSP);
            // pidDist.setInDB(0.5);
            // pidDist.setOutMn(0.2);
            // pidDist.setOutMx(pwrMx);
            // pidDist.setOutExp(1.0);
            // pidDist.setClamp(true);

            sqOrQT = false; //tank(1)/arcade(2)-apply sqrt | curvature(3)-quick turn
            diffType = 2;   //0-Off | 1=tank | 2=arcade | 3=curvature

            Drive.distRst();
            initSDB();
            state++;
            System.out.println("MOH - 0");
        case 1: // IF position error (hdg) GT errHdgLT turn first the MOH
            System.out.println("MOH - 1");
            trajCmd[0] = pidHdg.calculateX(hdgFB());   //cmd[0]=rotate(X), [1]=fwd(Y)
            trajCmd[1] = 0.0;
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2);    //Send to Drive system
            prtShtuff("MOH");
            if (Math.abs(pidHdg.getPositionError()) > errHdgLT ) break;   // Chk hdg error
            state++;
        case 2: // Move forward, steer Auto Heading and Dist
            System.out.println("MOH - 2");
            trajCmd[0] = pidHdg.calculateX(hdgFB());   //cmd[0]=rotate(X), [1]=fwd(Y)
            trajCmd[1] = pidDist.calculateX(distFB()); //cmd[0]=rotate(X), [1]=fwd(Y)
            sendDriveCmds(trajCmd[1], trajCmd[0], false, 2);
            // prtShtuff("MOH");
            if (!pidDist.atSetpoint() || !pidHdg.atSetpoint()) break;   // Chk both done
            state++;
        case 3: // Done
            setDone();  //Flag done and stop motors
            System.out.println("MOH - 3: ---------- Done -----------");
            break;
        default:
            setDone();  //Flag done and stop motors
            System.out.println("MOH - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
        // return strCmd;
    }
}
