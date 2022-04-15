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
public class TrajBrake extends ATrajFunction {

    // General
    private PIDXController pidSpd = pidHdg;
    private double spdSP = 0.0;         //Speed SP.
    private double pwrMx = 0.0;         //Max power to apply to pidDist. 
    private int opts = 3;               //Opts: bit0(1)-shootL, bit1(2)-shootR, bit3(4)-shoot drift always

    // dont use negative power - why?

    /**
     * Constructor
     * @param ePwr - Max power to apply to pidDist.
     */
    public TrajBrake(double ePwr) {
        pwrMx = Math.abs(ePwr);
    }

    public void execute() {
        switch (state) {
        case 0: // Init Trajectory for braking
            //Set extended values pidRef,    SP,       PB,  DB,  Mn,  Mx,   Exp, Clmp
            PIDXController.setExt(pidSpd, spdSP, (1.0/5), 2.0, 0.3, pwrMx, 1.0, true);

            sqOrQT = false; //tank(1)/arcade(2)-apply sqrt | curvature(3)-quick turn
            diffType = 1;   //0-Off | 1=tank | 2=arcade | 3=curvature

            state++;
            trajTmr.clearTimer();
            System.out.println("TBK - 0:");
        case 1: // Apply interted power to direction of travel (sign of RPM)
            System.out.println("TBK - 1: ------ Braking ------");
            trajCmd[0] = pidSpd.calculateX(distFPS());   //cmd[0]=brake cmd
            sendDriveCmds(trajCmd[0], trajCmd[0], sqOrQT, diffType);
            // prtShtuff("MOH1");
            if (pidSpd.atSetpoint()) state++;   // Chk stopped
            break;
        case 2: // Done
            setDone();  //Flag done and stop motors
            System.out.println("MOHS - 3: ----- Done -----");
            break;
        default:
            setDone();  //Flag done and stop motors
            System.out.println("MOHS - Dflt: ------  Bad state  ----");
            break;
        }
        updSDB();
        // return strCmd;
    }

}
