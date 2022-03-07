package frc.robot.subsystem.drive.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.Timer;
/*Author - Purab
 *History
 *2/24/22 - Initial release
 */

/**
 * This TrajFunction delays executionof the trajectory.
 */
public class TrajDelay extends ATrajFunction {

    private static Timer delayTimer;
    private double timeDelay;

    /**
     * Constructor to delay execution of the trajectory.
     * @param secDelay seconds to delay execution of the trajectory.
     */
    public TrajDelay(double secDelay) {
        Drive.cmdUpdate(0.0,0.0,false,0);
        timeDelay = secDelay;
        delayTimer = new Timer(timeDelay);
    }

    public void execute() {
        switch (state) {
        case 0: // set Snorfler control
            Drive.cmdUpdate(0.0,0.0,false,0); //TODO: if something with drive isn't working, probably remove this.
            delayTimer.clearTimer();
            state++;
        // System.out.println("Snf - 0: ---------- Init -----------");
            break;
        case 1:
            Drive.cmdUpdate(0.0,0.0,false,0); //TODO: if something with drive isn't working, probably remove this.
            if(delayTimer.hasExpired(timeDelay, true)) state++;
            break;
        case 2:
            setDone();
            // System.out.println("Snf - 1: ---------- Done -----------");
            break;
        default:
            setDone();
            System.out.println("Time Delay - Dflt: ------  Bad state  ----");
            break;
        }
    }
}
