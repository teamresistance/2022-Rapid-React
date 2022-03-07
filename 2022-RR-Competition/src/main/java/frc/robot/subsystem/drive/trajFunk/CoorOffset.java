package frc.robot.subsystem.drive.trajFunk;

import frc.io.hdw_io.IO;
import frc.robot.subsystem.drive.Drive;
import frc.util.PIDXController;

/**
 * This AutoFunction uses Arcade to turn to heading THEN moves distance.
 */
public class CoorOffset extends ATrajFunction {

    private double hdg_OS = 0.0;
    private double coorX_OS = 0.0;
    private double coorY_OS = 0.0;
    
    public CoorOffset(double hdg_OS, double coorX_OS, double coorY_OS) {
        this.hdg_OS = hdg_OS;
        this.coorX_OS = coorX_OS;
        this.coorY_OS = coorY_OS;
    }

    public void execute() {
        switch (state) {
        case 0: 
            IO.navX.setAngleAdjustment(hdg_OS);
            IO.coorXY.setXY_OS(coorX_OS, coorY_OS);
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