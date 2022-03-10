package frc.robot.subsystem;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Axis;
import frc.io.joysticks.util.Button;
import frc.robot.subsystem.drive.Drive;
import frc.util.Timer;


public class Shooter {
    // Hardware defintions:
    private static ISolenoid select_low_SV = IO.select_low_SV; // Defaults to high pressure; switches to low pressure.
    private static ISolenoid left_catapult_SV = IO.catapult_L_SV; // Left catapult trigger.
    private static ISolenoid right_catapult_SV = IO.catapult_R_SV; // Right catapult trigger.

    // Joystick buttons:
    private static Axis axSelLow = JS_IO.axGoalSel;
    private static Button btnFire = JS_IO.btnFire;
    private static Button btnReject_L = JS_IO.btnRejectLeft;
    private static Button btnReject_R = JS_IO.btnRejectRight;

    // Variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM.
    private static Timer stateTmr = new Timer(.05); // Timer for state machine
    private static boolean low_select = false; // Used to command the pressure SV. Default is hi press, switch.
    /** request from Drv_Auto to Shooter, resets itself */
    public static Boolean reqShootLowDrvAuto = null; // request from Drv_Auto to shoot 
    //null: not shooting, false: shooting high, true: shooting low

    /**
     * Initialize Shooter stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        btnFire.onButtonPressed();      //Clear button press on startup
        sdbInit();
        cmdUpdate(false, false, false); // select goal, left trigger, right trigger
        state = 0; // Start at state 0
        reqShootLowDrvAuto = null;      //null=no action, false= hi goal, true=lo goal
    }

    /**
     * Update Shooter. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        if (reqShootLowDrvAuto == null) {
            low_select = -axSelLow.get() < 0.10;
        } else if (state == 0) {
            state = 1;
            low_select = reqShootLowDrvAuto;
            // System.out.println("Shtr Auto" + reqShootLowDrvAuto);
        }
        if ((btnFire.onButtonPressed()) && state == 0) state = 1;

        if (btnReject_L.onButtonPressed() && state == 0) state = 11;

        if (btnReject_R.onButtonPressed() && state == 0) state = 13;

        smUpdate();
        sdbUpdate();
    }

    private static void smUpdate() { // State Machine Update
        switch (state) {
            case 0: // Everything is off, no pressure, pressure default high, Ltrig and Rtrig off.
                cmdUpdate(low_select, false, false);
                stateTmr.clearTimer(); // Initialize timer for covTrgr. Do nothing.
                break;
            case 1: // btn Fire, wait for prs settle
                cmdUpdate(low_select, false, false);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 2: // Fire left, wait
                cmdUpdate(low_select, true, false);
                if (stateTmr.hasExpired(0.6, state)) state++;
                break;
            case 3: // Left closed, wait
                cmdUpdate(low_select, true, false);
                if (stateTmr.hasExpired(0.0, state)) state++;
                break;
            case 4: // Fire right, return to 0, reset the reqShootDrvAuto 
                cmdUpdate(low_select, true, true);
                if (stateTmr.hasExpired(1.0, state)){
                    state = 0;
                    reqShootLowDrvAuto = null;
                }

                break;
            //-----------Reject Balls ---------------
            case 11: // Reject left with low prs, wait for settle
                cmdUpdate(true, false, false);
                if (stateTmr.hasExpired(0.5, state)) state++;
                break;
            case 12: // trigger left, wait and return to 0
                cmdUpdate(true, true, false); //tbd
                if (stateTmr.hasExpired(1.0, state)) state = 0;
                break;
            case 13: // Reject right with low prs, wait for settle
                cmdUpdate(true, false, false);
                if (stateTmr.hasExpired(0.5, state)) state++;
                break;
            case 14: // trigger right, wait and return to 0
                cmdUpdate(true, false, true);
                if (stateTmr.hasExpired(1.0, state)) state = 0;
                break;
            default: // all off
                cmdUpdate(true, false, false);
                System.out.println("Bad Shooter state: " + state);
                break;

        }
    }

    /**
     * Issue spd setting as rpmSP if isVelCmd true else as percent cmd.
     * 
     * @param select_low    - select the low goal, other wise the high goal
     * @param left_trigger  - triggers the left catapult
     * @param right_trigger - triggers the right catapult
     * 
     */
    private static void cmdUpdate(boolean select_low, boolean left_trigger, boolean right_trigger) {
        select_low_SV.set(select_low);
        left_catapult_SV.set(left_trigger);
        right_catapult_SV.set(right_trigger);
    }
    
    /*-------------------------  SDB Stuff --------------------------------------
    /**Initialize sdb */
    private static void sdbInit() {

    }

    /**Update sdb */
    private static void sdbUpdate() {
        SmartDashboard.putNumber("Shooter/state", state);
        SmartDashboard.putBoolean("Shooter/select_low_SV", select_low_SV.get());
        SmartDashboard.putBoolean("Shooter/left_catapult_SV", left_catapult_SV.get());
        SmartDashboard.putBoolean("Shooter/right_catapult_SV", right_catapult_SV.get());
        SmartDashboard.putBoolean("Shooter/low_select", low_select);
        SmartDashboard.putNumber("Shooter/level_select_input", -axSelLow.get());
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

}