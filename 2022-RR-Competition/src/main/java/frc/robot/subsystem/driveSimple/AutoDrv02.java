package frc.robot.subsystem.driveSimple;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.robot.subsystem.drive.Drive;
import frc.io.hdw_io.IO;
import frc.util.Timer;

/**
 * Example of straight with hdg chg.
 */
public class AutoDrv02 {
    // hdw defintions:

    // joystick buttons:

    // variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    /**
     * Called from autoInit in Robot.java
     */
    public static void init() {
        sdbInit();
        Drive.cmdUpdate(0.0, 0.0);
        state = 0; // Start at state 0
    }

    /**
     * Called from autoPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        //Add code here to start state machine or override the sm sequence
        smUpdate();
        sdbUpdate();
    }

    private static void smUpdate() { // State Machine Update

        switch (state) {
            case 0: // Everything is off
                Drive.cmdUpdate(0.0, 0.0);
                stateTmr.hasExpired(0.05, state); // Initialize timer for covTrgr. Do nothing.
                break;
            case 1: // Go straight for 1 sec.
                cmdUpdate(0.4, 0.4);
                if (stateTmr.hasExpired(1.0, state)) state++;
                break;
            case 2: // Spin Left to 90 degree heading using gyro.
                cmdUpdate(-0.4, 0.4);
                if (IO.navX.getAngle() > 80.0) state++;
                break;
            case 3: // Go straight for 1 sec.
                cmdUpdate(0.4, 0.4);
                if (stateTmr.hasExpired(1.0, state)) state++;
                break;
            case 4: // Spin Right for 1 sec.
                cmdUpdate(0.4, -0.4);
                if (IO.navX.getAngle() < 10.0) state++;
                break;
            case 5: // Stop, send 0, 0 cmds and stay here until end of auto.
                cmdUpdate(0.0, 0.0);
                // if (stateTmr.hasExpired(0.05, state)) state++;
                break;
            default: // all off
                cmdUpdate(0.0, 0.0);
                System.out.println("Bad DrvAuto01 state: " + state);
                break;

        }
    }

    /**
     * Issue percent cmd as tank steer.
     * 
     * @param lCmd - Left wheel cmd
     * @param rCmd - Right
    * 
     */
    private static void cmdUpdate(double lCmd, double rCmd) {
        //Check any safeties, mod passed cmds if needed.
        Drive.cmdUpdate(lCmd, rCmd);
    }

    /*-------------------------  SDB Stuff --------------------------------------
    /**Initialize sdb */
    private static void sdbInit() {
    }

    /**Update the Smartdashboard. */
    private static void sdbUpdate() {
        SmartDashboard.putNumber("AutoDrv/01. state", state);
    }

    // ----------------- Shooter statuses and misc.-----------------
    /**
     * Probably shouldn't use this bc the states can change. Use statuses.
     * 
     * @return - present state of Shooter state machine.
     */
    public static int getState() {
        return state;
    }

    /**
     * @return If the state machine is running, not idle.
     */
    public static boolean getStatus(){
        return state != 0;      //This example says the sm is runing, not idle.
    }

}