package frc.robot.subsystem.driveSimple;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.robot.subsystem.drive.Drive;
import frc.io.hdw_io.IO;
import frc.util.Timer;

/**
 * Example of straight with hdg chg.
 * <p>
 * Go straight for 5' to cross line and stay out of the way.
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
        // Drive.cmdUpdate(0.0, 0.0);       //Don't think we need this.  State 0 does the same.
        state = 0; // Start at state 0
    }

    /**
     * Called from autoPeriodic in robot.java.
     */
    public static void update() {
        //Add code here to start state machine or override the sm sequence
        smUpdate();
        sdbUpdate();
    }

    private static void smUpdate() { // State Machine Update
        //All off

        switch (state) {
            case 0: // Everything is off
                Drive.cmdUpdate(0.0, 0.0);
                stateTmr.hasExpired(0.05, state); // Initialize timer for covTrgr. Do nothing.
                break;
            case 1: // Go straight for 1 sec.
                cmdUpdate(-0.4, -0.5);
                if (stateTmr.hasExpired(1.0, state)) state++;
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