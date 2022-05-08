package frc.robot.subsystem.drive.trajFunk;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystem.Shooter;
import frc.util.Timer;

/**
 * This TrajFunction passes requests to the Shooter.
 */
public class ShootDrvAutoSave extends ATrajFunction {
    boolean low_select_L = true;
    boolean low_select_R = true;
    Timer shootTimer = new Timer(2.0);

    /**
     * Constructor to control the Shooter
     * @param low_select_L Left - null: no shot, false: shoot high, true: shoots low 
     * @param low_select_R Right - null: no shot, false: shoot high, true: shoots low 
     */
    public ShootDrvAutoSave(Boolean low_select_L, Boolean low_select_R) {
        this.low_select_L = low_select_L;
        this.low_select_R = low_select_R;
    }

    /**
     * Constructor to request the Shooter to launch both cargo.
     * @param low_select false: shoots both high, true: shoots both low 
     */
    public ShootDrvAutoSave(boolean low_select) {
        this.low_select_L = low_select;
        this.low_select_R = low_select;
    }

    public void execute() {
        // Drive.cmdUpdate();   //Shouldn't need this anymore.  Issues 0, 0 cmds by default
        switch (state) {
        case 0: // set shooter lo/hi control and time delay to allow shot
            // Shooter value starts null, set t/f here, then shooter resets to null when done
            // Shooter.reqShootLowDrvAuto = low_select_L; 
            Shooter.reqLowDA_L = low_select_L; 
            Shooter.reqLowDA_R = low_select_R; 
            shootTimer.startTimer(1.0);
            state++;
            System.out.println("Shoot - 0: ---------- Init -----------");
        case 1: // Wait for timer
            if(shootTimer.hasExpired()) state++;
            // if(Shooter.reqShootLowDrvAuto == null) state++; //maybe??
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
