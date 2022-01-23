package frc.robot.subsystem;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.Encoder;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.ISolenoid;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.Button;
import frc.util.Timer;

public class Shooter {
    // hdw defintions:
    private static ISolenoid select_low_SV = IO.select_low_SV; // Defaults to high pressure; switches to low pressure. 
    private static ISolenoid left_catapult_SV = IO.left_catapult_SV; // Left catapult trigger. 
    private static ISolenoid right_catpult_SV = IO.right_catpult_SV; // Right catapult trigger.
    // joystick buttons:
    private static Button btn_high_fire = JS_IO.btn_high_fire;
    private static Button btn_low_fire = JS_IO.btn_low_fire;
    // variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine
    private static boolean low_select = false; // Used to command the pressure SV. Default is hi press, switch.

    
    /**
     * Initialize Shooter stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();
        cmdUpdate(false, false, false); // Turn motor off(pct), injector false, revolver false
        state = 0; // Start at state 0
        }

    /** Update Shooter. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */

    private static void update() {
        if (btn_high_fire.onButtonPressed() && state == 0) { 
            state = 1; 
         } 
      
        if (btn_low_fire.onButtonPressed() && state == 0) { 
           state = 2; 
        } 
       

        smUpdate();
        sdbUpdate();
    }

    
    public static void smUpdate() {         // State Machine Update

        switch (state) {
            // Shooter disabled, flywheel off, injector Disabled, hood down, request
            // revolver, false.
            case 0: // Everything is off, no pressure, pressure default high, Ltrig and Rtrig off.
                low_select = false;
                cmdUpdate(low_select, false, false);
                stateTmr.hasExpired(0.05, state); // Initialize timer for covTrgr. Do nothing.
                break;
            case 1: // btn_high_fire
                low_select = false;
                state = 3;
                break;
            case 2: // btn_low_fire
                low_select = true;
                state = 3;
                break;
            case 3: // time delay to allow pressure to settle
               if(stateTmr.hasExpired(0.05, state)) state++;
                cmdUpdate(low_select, false, false);
                break;
            case 4: // trigger left
                cmdUpdate(low_select, true, false);
                break;
            case 5: // wait for left trigger
                if(stateTmr.hasExpired(0.1, state)) state++;
                cmdUpdate(low_select, true, false);
                break;
            case 6: // wait for recharge
                if(stateTmr.hasExpired(0.25, state)) state++;
                cmdUpdate(low_select, false, false);
                break;
            case 7: // trigger right
                cmdUpdate(low_select, false, true);
                break;
            case 8: // wait for right trigger
                if(stateTmr.hasExpired(0.1, state)) state = 0;
                cmdUpdate(low_select, false, true);
                break;
            default: // all off
                cmdUpdate(false, false, false);
                break;

        }
    }

    /**
     * Issue spd setting as rpmSP if isVelCmd true else as percent cmd.
     * 
     * @param spd      - cmd to issue to Flywheel Talon motor controller as rpm or
     *                 percentage
     * @param isVelCmd - spd should be issued as rpm setpoint else as a percenetage
     *                 output.
     * @param injCmd   - Request injector to start & stop.
     * @param revCmd   - Request revolver to index 1 time.
     */
    public static void cmdUpdate(boolean select_low, boolean left_trigger, boolean right_trigger) { 
        select_low_SV.set(select_low);
        left_catapult_SV.set(left_trigger);
        right_catpult_SV.set(right_trigger);
    }

    /*-------------------------  SDB Stuff --------------------------------------
    /**Initialize sdb */
    public static void sdbInit() {
        
    }

    public static void sdbUpdate() {
        SmartDashboard.putNumber("Shooter/state", state);
        SmartDashboard.putBoolean("Shooter/select_low_SV", select_low_SV.get());
        SmartDashboard.putBoolean("Shooter/left_catapult_SV", left_catapult_SV.get());
        SmartDashboard.putBoolean("Shooter/right_catapult_SV", right_catpult_SV.get());
        SmartDashboard.putBoolean("Shooter/low_select", low_select);
    }

    // ------------------------------ Shooter statuses and misc.-------------------------
    /**
     * Probably shouldn't use this bc the states can change. Use statuses.
     * 
     * @return - present state of Shooter state machine.
     */
    public static int getState() {
        return state;
    }


}