package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.Timer;

public class CurveTurnTm extends ATrajFunction {

    private double fwd = 0;
    private double rot = 0;
    private double time = 0;
    private Timer curveTime;

/**
 * This ATrajFunction uses Curvature drive to circle for a time period.
 * The rotation (JSX), fwd speed (JSY) and time period are passed.
 */
public CurveTurnTm(double eFwd, double eROT, double eTime) {
        fwd = eFwd;
        rot = eROT;
        time = eTime;
        curveTime = new Timer(eTime);
    }

    public void execute() {
        switch (state) {
        case 0:
            state++;
            break;
        case 1:
            // diffDrv.curvatureDrive(pwr, rot, false);
            // Drive.cmdUpdate(fwd, rot, false, 3);
            Drive.setDriveCmds(fwd, rot, false, 3);
            if (curveTime.hasExpired(time, state)) {
                state++;
            }
            break;
        case 2:
            setDone();
            break;
        default:
            setDone();
            System.out.println("Snf - Dflt: ------  Bad state  ----");
            break;
        }
    }
}
