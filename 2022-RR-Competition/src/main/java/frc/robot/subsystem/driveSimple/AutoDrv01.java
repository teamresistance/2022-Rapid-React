package frc.robot.subsystem.driveSimple;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.robot.subsystem.Shooter;
import frc.robot.subsystem.Snorfler;
import frc.robot.subsystem.drive.Drive;
import frc.io.hdw_io.IO;
import frc.util.Timer;

/**
 * Example of simple timed moves.
 * <p>
 * Aligned on 'D' ball to go straight, pick it up turn ~180 hdg
 * then go to fender and shoot.
 */
public class AutoDrv01 {
    // hdw defintions:

    // joystick buttons:

    // variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    /**
     * Called from autoInit in robot.java.
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

        switch (state) {
            case 0: // Everything is off
                cmdUpdate(0.0, 0.0);
                stateTmr.clearTimer();; // Initialize timer for covTrgr. Do nothing.
                System.out.println("AutoDrv01 Init.");
                state++;
                break;
            case 1:
                Snorfler.reqsnorfDrvAuto = true;
                IO.navX.reset();
                state++;
                break;
            case 2: // Go straight fwd for 1 sec (4.7') to ball.
                cmdUpdate(-0.4, -0.5);     //  4.7'/sec @ (-0.4, -0.5)
                if (stateTmr.hasExpired(1.35, state)) state++;
                break;
            case 3: // Coastout.
                cmdUpdate(0.0, 0.0);
                if (stateTmr.hasExpired(0.2, state)) state++;
                break;
            case 4: // Brake.
                // cmdUpdate(0.3, 0.4);
                // if (stateTmr.hasExpired(0.3, state)) state++;
                state++;
                break;
            case 5: // Pivot CCW to 180 hdg
                cmdUpdate(0.3, -0.6);     //4.7/sec @ 0.4, 0.5
                if (IO.navX.getAngle() < -138.0) state++;
                // if (stateTmr.hasExpired(1.25, state)) state++;
                break;
            case 6: // Coastout.
                cmdUpdate(0.0, 0.0);
                if (stateTmr.hasExpired(0.2, state)) state++;
                break;
            case 7: // Brake.
                state++;
                break;
                // cmdUpdate(0.3, -0.5);
                // if (stateTmr.hasExpired(0.3, state)) state++;
                // break;
            case 8: // Start the Snofler
                Snorfler.reqsnorfDrvAuto = false;
                state++;
                break;
            case 9: // Go straight fwd for 1 sec (4.7') to goal.
                cmdUpdate(-0.4, -0.5);     //4.7/sec @ 0.4, 0.5
                if (stateTmr.hasExpired(2.3, state)) state++;
                break;
            case 10: // Coastout.
                cmdUpdate(0.0, 0.0);
                if (stateTmr.hasExpired(0.2, state)) state++;
                break;
            case 11: // Brake.
                // cmdUpdate(0.3, 0.4);
                // if (stateTmr.hasExpired(0.3, state)) state++;
                state++;
                break;
            case 12:    // Shoot
                Shooter.reqLowDA_L = false;
                state = 20;
                break;
            case 20: // Stop, send 0, 0 cmds and stay here until end of auto.
                cmdUpdate(0.0, 0.0);
                // if (stateTmr.hasExpired(0.05, state)) state++;
                break;
            default: // allAA off
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
        // Drive.cmdUpdate(lCmd, rCmd);
        Drive.setDriveCmds(lCmd, rCmd, false, 1);   //Tank steer, no squaring.
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