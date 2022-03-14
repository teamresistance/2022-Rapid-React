package frc.robot.subsystem.driveSave.trajFunk;

import frc.robot.subsystem.drive.Drive;
import frc.util.*;

/**
 * This AutoFunction uses Tank drive to turn to heading.
 */
public class TankTimed extends ATrajFunction {

    private double lPwr = 0.0;
    private double rPwr = 0.0;
    private Timer timer = new Timer();

    // dont use negative power - why?
    public TankTimed(double time, double _lPwr, double _rPwr) {
        this.lPwr = _lPwr;
        this.rPwr = _rPwr;
        timer.startTimer(time);
    }

    public void execute() {
        switch(state){
            case 0:
                timer.startTimer();
                state++;
                System.out.println("TT - 0: ---------- Init -----------");
            case 1:
                Drive.cmdUpdate(-lPwr, -rPwr,false, 1);
                if(timer.hasExpired()) state++;
                break;
            case 2:
                setDone();
                System.out.println("TT - 2: ---------- Done -----------");
                break;           
            default:
                setDone();
                System.out.println("TT - Dflt: ------  Bad state  ----");
                break;
        }
        updSDB();
    }
}