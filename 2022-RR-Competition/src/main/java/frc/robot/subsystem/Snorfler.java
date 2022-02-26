package frc.robot.subsystem;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.motorcontrol.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.util.*;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Button;
import frc.robot.Robot;
import frc.util.Timer;

public class Snorfler {
    // hdw defintions:
    private static ISolenoid snorflerExt_SV = IO.snorflerExt_SV;
    public static Victor snorfFeed_Mtr = IO.snorfFeed_Mtr; // Feed motor on snorfler
    public static Victor snorfElvLo_Mtr = IO.snorfElvLo_Mtr; // Lower elevator motor
    public static Victor snorfElvHi_Mtr = IO.snorfElvHi_Mtr; // High elevator motor

    //Defines RevRobotics 3 color sensor and RGB colors to sensr ball colors, Red & Blue.
    public static ColorSensorV3 ballColorSensor = IO.ballColorSensor;
    public static final ColorMatch colorMatcher = new ColorMatch();
    private static final Color kBlueTarget = new Color(0.17, 0.41, 0.41); // blue: 17 41 41
    private static final Color kGreenTarget = new Color(0.197, 0.561, 0.240);
    private static final Color kRedTarget = new Color(0.49, 0.35, 0.15); // red 49 35 15
    private static final Color kYellowTarget = new Color(0.361, 0.524, 0.113);

    // joystick buttons:
    private static Button btnSnorfle = JS_IO.btnSnorfle;
    private static Button btnRejectSnorfle = JS_IO.btnRejectSnorfle;

    // variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer snorfTimer = new Timer(0.1);
    private static Timer colorTimer = new Timer(0.1);
    public static boolean reqsnorfDrvAuto; // Request to enable the snorfler from Drv Auto system

    public static Color detectedColor;
    public static String colorString;
    public static ColorMatchResult match;
    private static String teamColor;
    private static String enemyColor;

    public static enum dirSnorfler { OFF, FWD, REJ }

    /**
     * Initialize Shooter stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();
        cmdUpdate(false,dirSnorfler.OFF); // select goal, left trigger, right trigger
        state = 0; // Start at state 0
        reqsnorfDrvAuto = false;

        colorMatcher.addColorMatch(kBlueTarget);
        colorMatcher.addColorMatch(kGreenTarget);
        colorMatcher.addColorMatch(kRedTarget);
        colorMatcher.addColorMatch(kYellowTarget);

    }

    /**
     * Update Shooter. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        teamColor = Robot.teamColorchsr.getSelected();      //Driver choosen team color
        enemyColor = teamColor == "Blue" ? "Red" : "Blue";  //The bad guy's color.
        
        if (match.color == kBlueTarget) {
            colorString = "Blue";
        } else if (match.color == kRedTarget) {
            colorString = "Red";
        } else if (match.color == kGreenTarget) {
            colorString = "Green";
        } else if (match.color == kYellowTarget) {
            colorString = "Yellow";
        } else {
            colorString = "Unknown";
        }

        detectedColor = ballColorSensor.getColor();
        match = colorMatcher.matchClosestColor(detectedColor);

        // if button down and auto snorf, snorf ball            
        // if not button, stop snorfling                        
        // if rejecting ball or see enemy color, reject ball    
        // if not rejecting ball and done with rejecting, normal

        if ((btnSnorfle.isDown() || reqsnorfDrvAuto) && state == 0)  state = 1; // Starts the state machine
        if ((btnSnorfle.isUp() && !reqsnorfDrvAuto) && state != 0) state = 0;
        if ((btnRejectSnorfle.isDown() || colorString.equals(enemyColor)))  state = 3;
        if (btnRejectSnorfle.isUp() && state == 4) state = 0; // Goes back to off

        smUpdate();
        sdbUpdate();
    
    }

    /**State Machine Update */
    private static void smUpdate() { // State Machine Update

        switch (state) {
            case 0: // Everything is off
                cmdUpdate(false, dirSnorfler.OFF);
                break;
            case 1: // Waiting a bit before forward
                cmdUpdate(true,dirSnorfler.OFF);
                if (snorfTimer.hasExpired(0.1,state)) {
                    state++;
                }
                break;
            case 2: // Start snorfler forward
                cmdUpdate(true, dirSnorfler.FWD);
                break;
            case 3: // Waiting a bit before going back
                cmdUpdate(true, dirSnorfler.OFF);
                if (snorfTimer.hasExpired(0.1,state)) {
                    state++;
                }
                break;  
            case 4: //Go back and reject
                cmdUpdate(true, dirSnorfler.REJ);
                break;           
            default: // all off
                cmdUpdate(false,dirSnorfler.OFF);
                System.out.println("bad snorfle state: " + state);
                break;
        }
    }

    /**
     * 
     * @param snorfEna - drops the snorfler arm, turns on all motors
     * 
     */
    private static void cmdUpdate(boolean snorfEna, dirSnorfler direction) {
        snorflerExt_SV.set(snorfEna);

        switch (direction) {
            case OFF:
                snorfFeed_Mtr.set(0.0);     
                snorfElvLo_Mtr.set(0.0);
                snorfElvHi_Mtr.set(0.0);
                break;
            case FWD:
                snorfFeed_Mtr.set(0.7); 
                snorfElvLo_Mtr.set(0.5);
                snorfElvHi_Mtr.set(0.5);
                break;
            case REJ:
                snorfFeed_Mtr.set(-0.7); 
                snorfElvLo_Mtr.set(-0.5);
                snorfElvHi_Mtr.set(-0.5);
                break;
        }
    }
    
    /**Check if the color of the ball is OK to pass, our team color. */
    private static boolean checkColorOK() {
        return !(colorTimer.hasExpired(0.1, colorString.equals(enemyColor)));
    }

    /*-------------------------  SDB Stuff --------------------------------------
    /**Initialize sdb */
    private static void sdbInit() {
        // Put stuff here on the sdb to be retrieved from the sdb later
        // SmartDashboard.putBoolean("ZZ_Template/Sumpthin", sumpthin.get());
    }

    /** Update the Smartdashboard. */
    private static void sdbUpdate() {
        // Put stuff to retrieve from sdb here. Must have been initialized in sdbInit().
        // sumpthin = SmartDashboard.getBoolean("ZZ_Template/Sumpthin", sumpthin.get());

        // Put other stuff to be displayed here
        SmartDashboard.putNumber("Snorfler/state", state);
        SmartDashboard.putNumber("Red", detectedColor.red);
        SmartDashboard.putNumber("Green", detectedColor.green);
        SmartDashboard.putNumber("Blue", detectedColor.blue);
        SmartDashboard.putString("Detected Color", colorString);

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
    public static boolean getStatus() {
        return state != 0; // This example says the sm is runing, not idle.
    }

}