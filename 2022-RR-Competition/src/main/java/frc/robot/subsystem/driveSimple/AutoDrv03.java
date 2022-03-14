package frc.robot.subsystem.driveSimple;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.robot.subsystem.Shooter;
import frc.robot.subsystem.Snorfler;
import frc.robot.subsystem.drive.Drive;
import frc.robot.subsystem.drive.Drv_Auto;
import frc.io.hdw_io.IO;
import frc.util.Timer;

/**
 * Example of simple timed moves.     
 * <p>
 * Shoot the 1 ball.  Backup and get 2nd ball then return to fender and shoot it.
 */
public class AutoDrv03 {
    //Picks up a and b balls

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
        System.out.println();
    }

    private static void smUpdate() { // State Machine Update

        switch (state) {
            case 0: // Everything is off
                Drive.cmdUpdate(0.0, 0.0);
                stateTmr.clearTimer();; // Initialize timer for covTrgr. Do nothing.
                state++;
                break;
            case 1:// Shoots exhisting ball
                Shooter.reqShootLowDrvAuto = false;
                if (stateTmr.hasExpired(1.0, state))state++;
                break;
            case 2: //Backs out
                Drive.cmdUpdate(0.4, 0.5);
                if (stateTmr.hasExpired(2.0, state)) state++;
                break;
            case 3: //Coastout
                cmdUpdate(0.0, 0.0);
                if (stateTmr.hasExpired(0.2, state)) state++;
                break;
            case 4:
                Drive.cmdUpdate(-0.2, 0.5);
                if (IO.navX.getAngle() > 160) state++;
                break;
            case 5:
                Snorfler.reqsnorfDrvAuto = true;
                state++;
                break;
            case 6: // Go straight fwd for 1 sec (4.7') to ball.
                cmdUpdate(-0.2, -0.3);     //  4.7'/sec @ (-0.4, -0.5)
                if (stateTmr.hasExpired(1.0, state)) state++;
                break;
            case 7: // Coastout.
                cmdUpdate(0.0, 0.0);
                if (stateTmr.hasExpired(0.2, state)) state++;
                break;
            case 8: //Turns towards b ball
                cmdUpdate(-0.5, 0.2);
                if (IO.navX.getAngle() > 220) state++;
                break;
            case 9: //drives towards b ball
                cmdUpdate(-0.2, -0.3);
                if (stateTmr.hasExpired(3, state)) state++;
                break;
            case 10: //Coastout
                cmdUpdate(0.0, 0.0);
                if (stateTmr.hasExpired(0.2, state)) state++;
                break;
            case 11: //Snorf up
                Snorfler.reqsnorfDrvAuto = false;
                state++;
                break;
            case 12: //Turns
                cmdUpdate(-0.5, 0.2);
                if (IO.navX.getAngle() > 360) state++;
                break;
            case 13: //Drives forward
                cmdUpdate(-0.4, -0.5);
                if (stateTmr.hasExpired(3, state)) state++;
                break;
            case 14: //Sqaure up on wall
                cmdUpdate(-0.2, -0.25);
                if (stateTmr.hasExpired(1, state)) state++;
                break;
            case 15:    // Shoot
                Shooter.reqShootLowDrvAuto = false;
                state = 20;
            case 20: // Stop, send 0, 0 cmds and stay here until end of auto.
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