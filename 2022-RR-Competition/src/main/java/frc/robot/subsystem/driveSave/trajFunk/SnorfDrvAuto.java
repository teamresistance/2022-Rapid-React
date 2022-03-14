package frc.robot.subsystem.driveSave.trajFunk;

import frc.robot.subsystem.Snorfler;
import frc.robot.subsystem.drive.Drive;

// import frc.robot.subsystem.ballHandler.Snorfler; //---- Commented out 2022 ----

/**
 * This TrajFunction controls the Snorfler.
 */
public class SnorfDrvAuto extends ATrajFunction {

    private boolean snorfEna = false;

    /**
     * Constructor to control the Snorfler, true = enable, false disable.
     * @param _snorfEna lower snorfler and start sucking up balls
     */
    public SnorfDrvAuto(boolean _snorfEna) {
        snorfEna = _snorfEna;
    }

    public void execute() {
        // Drive.cmdUpdate();
        switch (state) {
        case 0: // set Snorfler control true = enable, false disable
            Snorfler.reqsnorfDrvAuto = snorfEna;
            state++;
            // System.out.println("Snf - 0: ---------- Init -----------");
        case 1:
            setDone();
            System.out.println("Snf - 1: ---------- Done -----------");
            break;
        default:
            setDone();
            System.out.println("Snf - Dflt: ------  Bad state  ----");
            break;
        }
    }
}
