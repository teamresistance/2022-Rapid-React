package frc.robot.subsystem;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Button;
import frc.util.Timer;
import com.playingwithfusion.CANVenom;
public class Snorfler {
    // hdw defintions:
    private static CANVenom snoflerMotor; 
    private static ISolenoid snofleArm;

    // joystick buttons:
    private static Button btnSnorfleTgl = JS_IO.btnTglSnorfler;

    // variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    /**
     * Initialize Shooter stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();
        cmdUpdate(0.0, false); // select goal, left trigger, right trigger
        state = 0; // Start at state 0
    }

    /**
     * Update Shooter. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        //Add code here to start state machine or override the sm sequence
        smUpdate();
        sdbUpdate();
        if (btnSnorfleTgl.onButtonPressed()) state = state == 0 ? 1 : 0;
    }    

    private static void smUpdate() { // State Machine Update

        switch (state) {
            case 0: // Everything is off
                cmdUpdate(0.0, false);
                stateTmr.hasExpired(0.05, state); // Initialize timer for covTrgr. Do nothing.
                break;
            case 1: // Put arm down, wait for action
                cmdUpdate(0.0, true);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 2: // Start collector
                cmdUpdate(0.7, true);
                break;
            default: // all off
                System.out.println("Bad Snorfler state: " + state);
                cmdUpdate(0.0, false);
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
    private static void cmdUpdate(double mtrcmd, boolean armcmd) {
        //Check any safeties, modify passed cmds if needed.
        //Send commands to hardware
        snoflerMotor.set(mtrcmd);
        snofleArm.set(armcmd);

    }
    /*-------------------------  SDB Stuff --------------------------------------
    /**Initialize sdb */
    private static void sdbInit() {
        //Put stuff here on the sdb to be retrieved from the sdb later
        // SmartDashboard.putBoolean("ZZ_Template/Sumpthin", sumpthin.get());
    }

    /**Update the Smartdashboard. */
    private static void sdbUpdate() {
        SmartDashboard.putNumber("Snofler/state", state);
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