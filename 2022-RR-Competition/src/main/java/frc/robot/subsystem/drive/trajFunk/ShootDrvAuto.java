package frc.robot.subsystem.drive.trajFunk;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystem.Shooter;
import frc.util.Timer;

/**
 * This TrajFunction controls the Shooter.
 */
public class ShootDrvAuto extends ATrajFunction {
    boolean low_select = true;
    Timer shootTimer = new Timer(2.0);

    /**
     * Constructor to control the Shooter
     * @param low_select false: shoots high, true: shoots low 
     */
    public ShootDrvAuto(boolean low_select) {
        this.low_select = low_select;
    }

    public void execute() {
        switch (state) {
        case 0: // set shooter lo/hi control and time delay to allow shot
            Shooter.reqShootLowDrvAuto = low_select; // shooter resets it
            shootTimer.startTimer(3.0);
            state++;
            // System.out.println("Shoot - 0: ---------- Init -----------");
        case 1: // Wait for timer
            if(shootTimer.hasExpired()) state++;
            SmartDashboard.putNumber("Traj/ShootDelay", shootTimer.getRemainingSec());
            break;
        case 2:
            setDone();
            System.out.println("Shoot - 1: ---------- Done -----------");
            break;
        default:
            setDone();
            System.out.println("Shoot - Dflt: ------  Bad state  ----");
            break;
        }
    }
}
