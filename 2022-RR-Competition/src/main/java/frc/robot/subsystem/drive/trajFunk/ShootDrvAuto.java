package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.Shooter;

/**
 * This TrajFunction controls the Shooter.
 */
public class ShootDrvAuto extends ATrajFunction {
    boolean low_select = true;

    /**
     * Constructor to control the Shooter
     * @param low_select false: shoots high, true: shoots low 
     */
    public ShootDrvAuto(boolean low_select) {
        this.low_select = low_select;
    }

    public void execute() {
        switch (state) {
        case 0: // set shooter control
            Shooter.reqShootLowDrvAuto = this.low_select; // shooter resets it
            state++;
            System.out.println("Shoot - 0: ---------- Init -----------");
        case 1:
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
