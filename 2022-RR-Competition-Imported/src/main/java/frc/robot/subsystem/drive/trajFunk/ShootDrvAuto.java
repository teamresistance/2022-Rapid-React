package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.Shooter;
// import frc.util.Timer;

/**
 * This TrajFunction passes requests to the Shooter.
 */
public class ShootDrvAuto extends ATrajFunction {
    Boolean low_select_L = null;    //Left - null: no shot, false: shoot high, true: shoots low 
    Boolean low_select_R = null;    //Right - null: no shot, false: shoot high, true: shoots low
    // Timer shootTimer = new Timer(2.0);

    /**
     * Constructor to control the Shooter
     * <p>If only 1 parm passed shoots both.
     * @param low_select_L Left - null: no shot, false: shoot high, true: shoots low 
     * @param low_select_R Right (Opt.) - null: no shot, false: shoot high, true: shoots low 
     */
    public ShootDrvAuto(Boolean low_select_L, Boolean low_select_R) {
        this.low_select_L = low_select_L;
        this.low_select_R = low_select_R;
    }

    /**
     * Constructor to request the Shooter to launch both cargo.
     * @param low_select false: shoots both high, true: shoots both low 
     */
    public ShootDrvAuto(boolean low_select) {
        this.low_select_L = low_select;
        this.low_select_R = low_select;
    }

    public void execute() {
        switch (state) {
        case 0: // set shooter lo/hi control and time delay to allow shot
            // Shooter value starts null, set t/f here, then shooter resets to null when done
            Shooter.reqLowDA_L = low_select_L;
            Shooter.reqLowDA_R = low_select_R;
            state++;
            System.out.println("Shoot - 0: ---------- Init -----------");
        case 1: // Wait for timer
            // if(shootTimer.hasExpired()) state++;
            if(Shooter.reqLowDA_L == null && Shooter.reqLowDA_R == null) state++; //maybe??
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
