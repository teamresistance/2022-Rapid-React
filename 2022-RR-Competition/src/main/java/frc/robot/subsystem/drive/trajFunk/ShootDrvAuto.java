package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.Shooter;

/**
 * This TrajFunction controls the Shooter.
 */
public class ShootDrvAuto extends ATrajFunction {


    /**
     * Constructor to control the Shooter
     * @param _shootEna Shoot for high goal. Resets itself
     */
    public ShootDrvAuto() {
    }

    public void execute() {
        switch (state) {
        case 0: // set shooter control
            Shooter.reqShootDrvAuto = true; // shooter resets it
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
