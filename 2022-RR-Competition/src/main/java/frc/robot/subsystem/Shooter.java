package frc.robot.subsystem;

import javax.print.attribute.standard.Fidelity;

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
    private static double loSelTm = 0.0;  //Settle time for Lo/Hi switch
    private static double fireTm = 0.6;   //Recharge time after a fire
    private static boolean low_select = false; // Used to command the pressure SV. Default is hi press, switch.

    /** request from Drv_Auto to Shooter, resets itself
     * <p>null: not shooting, false: shooting high, true: shooting low 
     * <p>Shooter resets value to null when done executing. */
    public static Boolean reqLowDA_L = null; // request from Drv_Auto to shoot 
    /** request from Drv_Auto to Shooter, resets itself
     * <p>null: not shooting, false: shooting high, true: shooting low 
     * <p>Shooter resets value to null when done executing. */
    public static Boolean reqLowDA_R = null; // request from Drv_Auto to shoot 

    /**
     * Initialize Shooter stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        btnFire.onButtonPressed();      //Clear button press on startup
        sdbInit();
        cmdUpdate(false, false, false); // select goal, left trigger, right trigger
        state = 0; // Start at state 0
        reqLowDA_L = null;      //null=no action, false= hi goal, true=lo goal
        reqLowDA_R = null;      //null=no action, false= hi goal, true=lo goal
    }

    /**
     * Update Shooter. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        if(state == 0){     //Only evaluate when others finished, state 0
            if(reqLowDA_L == null && reqLowDA_R == null){   //If no DrvAuto requests
                // Handle Telop requests
                low_select = axSelLow.get() > 0.10;

                if (btnFire.onButtonPressed()) state = 1;
                if (btnReject_L.onButtonPressed()) state = 11;
                if (btnReject_R.onButtonPressed()) state = 13;
            }else{
                // Handle DrvAuto requests, fire left then right if requested.
                if(reqLowDA_R != null){ low_select = reqLowDA_R; state = 33;}
                if(reqLowDA_L != null){ low_select = reqLowDA_L; state = 31;}
            }
        }

        smUpdate();
        sdbUpdate();
    }

    /** State Machine update for shooter  */    
    private static void smUpdate() { // State Machine Update
        switch (state) {
            case 0: // Everything is off, no pressure, pressure default high, Ltrig and Rtrig off.
                cmdUpdate(low_select, false, false);
                stateTmr.clearTimer(); // Initialize timer for covTrgr. Do nothing.
                break;
            case 1: // btn Fire, wait for prs settle
                cmdUpdate(low_select, false, false);
                if (stateTmr.hasExpired(loSelTm, state)) state++;
                break;
            case 2: // Fire left, wait
                cmdUpdate(low_select, true, false);
                if (stateTmr.hasExpired(fireTm, state)) state++;
                break;
            case 3: // Left closed, wait
                cmdUpdate(low_select, true, false);
                if (stateTmr.hasExpired(loSelTm, state)) state++;
                break;
            case 4: // Fire right, return to 0, reset the reqShootDrvAuto 
                cmdUpdate(low_select, true, true);
                if (stateTmr.hasExpired(fireTm, state)){
                    state = 0;
                }
                break;
            //-----------Reject Balls ---------------
            case 11: // Reject left with low prs, wait for settle
                cmdUpdate(true, false, false);
                if (stateTmr.hasExpired(loSelTm, state)) state++;
                break;
            case 12: // trigger left, wait and return to 0
                cmdUpdate(true, true, false); //tbd
                if (stateTmr.hasExpired(fireTm, state)) state = 0;
                break;
            case 13: // Reject right with low prs, wait for settle
                cmdUpdate(true, false, false);
                if (stateTmr.hasExpired(loSelTm, state)) state++;
                break;
            case 14: // trigger right, wait and return to 0
                cmdUpdate(true, false, true);
                if (stateTmr.hasExpired(fireTm, state)) state = 0;
                break;
            //----- DrvAuto request ---------------
            case 31: // DrvAuto req to fire left, wait for lo/hi select
                cmdUpdate(low_select, false, false);
                if (stateTmr.hasExpired(loSelTm, state)) state++;
                break;
            case 32:    // DrvAuto req to fire left, wait for fire prs settle
                cmdUpdate(low_select, true, false);
                reqLowDA_L = null;
                if (stateTmr.hasExpired(fireTm, state)) state = 0;
                break;
            case 33: // DrvAuto req to fire right, wait for lo/hi select
                cmdUpdate(low_select, false, false);
                if (stateTmr.hasExpired(loSelTm, state)) state++;
                break;
            case 34:    // DrvAuto req to fire right, wait for prs settle
                cmdUpdate(low_select, false, true);
                reqLowDA_R = null;
                if (stateTmr.hasExpired(fireTm, state)) state = 0;
                break;
            default: // all off
                cmdUpdate(true, false, false);
                System.out.println("Bad Shooter state: " + state);
                break;
        }
    }

    /**
     * Issue all cmds for shooter.  Include any safeties here.
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
    /**Initialize sdb
     * <p>--- Note: due to CAN buss issues, any CAN object is commented out
     * unless troubleshooting. ---
     */
    private static void sdbInit() {
        SmartDashboard.putNumber("Shooter/4. LoHi Select Time", loSelTm);
        SmartDashboard.putNumber("Shooter/5. Post Fire Time", fireTm);
    }

    /**Update sdb
     * <p>--- Note: due to CAN buss issues, any CAN object is commented out
     * unless troubleshooting. ---
     */
    private static void sdbUpdate() {
        SmartDashboard.putNumber("Shooter/1. state", state);
        SmartDashboard.putBoolean("Shooter/2. low_select", low_select);
        SmartDashboard.putNumber("Shooter/3. level_select_input", -axSelLow.get());
        loSelTm = SmartDashboard.getNumber("Shooter/4. LoHi Select Time", loSelTm);
        fireTm = SmartDashboard.getNumber("Shooter/5. Post Fire Time", fireTm);
        // SmartDashboard.putBoolean("Shooter/6. reqLowDA_L", reqLowDA_L);  //can't handle null
        // SmartDashboard.putBoolean("Shooter/7. reqLowDA_R", reqLowDA_R);
        // SmartDashboard.putBoolean("Shooter/10. select_low_SV", select_low_SV.get());            //CAN
        // SmartDashboard.putBoolean("Shooter/11. left_catapult_SV", left_catapult_SV.get());      //CAN
        // SmartDashboard.putBoolean("Shooter/12. right_catapult_SV", right_catapult_SV.get());    //CAN
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