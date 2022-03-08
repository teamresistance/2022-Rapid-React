package frc.robot.subsystem;

import java.net.SocketTimeoutException;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.motorcontrol.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
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
    public static Victor snorfElv_Mtrs = IO.snorfElv_Mtrs; // Upper & Lower elevator motor

    //Defines RevRobotics 3 color sensor and RGB colors to sensr ball colors, Red & Blue.
    public static ColorSensorV3 ballColorSensor;
    public static final ColorMatch colorMatcher = new ColorMatch();
    private static final Color kBlueTarget = new Color(0.17, 0.41, 0.41); // blue: 17 41 41
    private static final Color kGreenTarget = new Color(0.197, 0.561, 0.240);
    private static final Color kRedTarget = new Color(0.49, 0.35, 0.15); // red 49 35 15
    private static final Color kYellowTarget = new Color(0.361, 0.524, 0.113);

    // joystick buttons:
    private static Button btnSnorfle = JS_IO.btnSnorfle;
    private static Button btnRejectSnorfle = JS_IO.btnRejectSnorfle;
    private static Button btnBadColor = JS_IO.btnBadColor;

    // variables:
    private static int state; // Shooter state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer snorfTimer = new Timer(0.1);
    private static Timer colorTimer = new Timer(0.1);
    private static Timer holdoutTimer = new Timer(0.1);
    public static boolean reqsnorfDrvAuto; // Request to enable the snorfler from Drv Auto system

    public static Color detectedColor;
    public static String colorString;
    public static ColorMatchResult match;
    private static String teamColor;
    private static String enemyColor;
    private static boolean csBallReject = false;  // ??

    public static enum dirSnorfler { OFF, FWD, REJ }

    /**
     * Initialize Shooter stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();
        cmdUpdate(false, 0.0, 0.0); // select goal, left trigger, right trigger
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
        //Bad color Btn for testing the color sensor logic w/o a color sensor.
        if (btnBadColor.isDown()) colorString = enemyColor;
        // ballColorUpdate();

        // if button down and auto snorf, snorf ball
        // if not button, stop snorfling
        // if rejecting ball or see enemy color, reject ball
        // if not rejecting ball and done with rejecting, normal

        if (/*!colorString.equals(enemyColor)*/ true && !csBallReject) {
            if ((btnSnorfle.isDown() || reqsnorfDrvAuto) && (state == 0 || state == 22 || state == 30))  state = 1; // Starts the snorfling
            if ((btnSnorfle.isUp() && !reqsnorfDrvAuto) &&
                (state != 0 && state != 12 && state != 30)) state = 30;//Stop snorf but elv for 2 sec.
            if (btnRejectSnorfle.isDown() && (state < 11 || state == 22 || state == 30))  state = 11; // Start Manual Rejection
            if (btnRejectSnorfle.isUp() && state == 12) state = 0;   // Stop Manual Rejection
        } else if (!csBallReject) {
            System.out.println("State 21");
            csBallReject = true;
            state = 21;
        }
        smUpdate();
        sdbUpdate();
    }

    /**State Machine Update */
    private static void smUpdate() { // State Machine Update

        switch (state) {
            case 0: // Everything is off
                cmdUpdate(false, 0.0, 0.0);
                snorfTimer.clearTimer();
                break;
            //--------- Manual Snorfle -------------
            case 1: // Waiting a bit before forward
                cmdUpdate(true,0.0, 0.0);
                if (snorfTimer.hasExpired(0.1,state)) {
                    state++;
                }
                break;
            case 2: // Start snorfler forward
                cmdUpdate(true, 0.7, 1.0);
                break;
            //--------- Manual Reject -------------
            case 11: // Waiting a bit before going back
                cmdUpdate(true, 0.0, 0.0);
                if (snorfTimer.hasExpired(0.1,state)) {
                    state++;
                }
                break;
            case 12: //Go back and reject
                cmdUpdate(true, -0.7, -1.0);
                break;           
            //--------- Color Sensor Reject -------------
            case 21: // Waiting a bit before going back
                cmdUpdate(true, 0.0, 0.0);
                if (snorfTimer.hasExpired(0.1,state)) state++;
                break;
            case 22: //Go back and reject for 2.0 seconds
                cmdUpdate(true, -0.7, -1.0);
                System.out.println("Here");
                if (snorfTimer.hasExpired(4.0, true)){
                    System.out.println("Here1");
                    csBallReject = false;
                    state = 0;
                }
                break; 

            case 30: //Finish snorf but keep elv for 2 sec longer
                cmdUpdate(false, 0.0, 1.0);
                if (snorfTimer.hasExpired(2.0, state)){
                    state = 0;
                    csBallReject = false;
                }
                break;           
            default: // all off
                cmdUpdate(false, 0.0, 0.0);
                System.out.println("Bad snorfle state: " + state);
                break;
        }
    }

    /**
     * 
     * @param snorfEna - drops the snorfler arm, turns on all motors
     * 
     */
    private static void cmdUpdate(boolean snorfEna, double mtrSpd, double elvSpd) {
        snorflerExt_SV.set(snorfEna);
        snorfFeed_Mtr.set(mtrSpd);
        snorfElv_Mtrs.set(elvSpd);
    }
    
    /**Check if the color of the ball is OK to pass, our team color. */
    private static boolean checkColorOK() {
        return !(colorTimer.hasExpired(0.1, colorString.equals(enemyColor)));
    }

    /*-------------------------  SDB Stuff --------------------------------------
    
    /**Initialize sdb */

    public static SendableChooser<String> teamColorchsr = new SendableChooser<String>();
    private static String[] chsrDesc = { "Blue", "Red", "none" };

    
    /**Initialize Traj chooser */
    public static void teamColorchsrInit(){
        for(int i = 0; i < chsrDesc.length; i++){
            teamColorchsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        teamColorchsr.setDefaultOption(chsrDesc[0] + " (Default)", chsrDesc[0]);   //Default MUST have a different name
        SmartDashboard.putData("Robot/TeamColor", teamColorchsr);
    }

    /**Show on sdb traj chooser info.  Called from robotPeriodic  */
    public static void teamColorchsrUpdate(){
        SmartDashboard.putString("Robot/TeamColorChoosen", teamColorchsr.getSelected());
    }


    private static void sdbInit() {
        // Put stuff here on the sdb to be retrieved from the sdb later
        // SmartDashboard.putBoolean("ZZ_Template/Sumpthin", sumpthin.get());
    }

    /** Update the Smartdashboard. */
    private static void sdbUpdate() {
        // Put stuff to retrieve from sdb here. Must have been initialized in sdbInit().
        // sumpthin = SmartDashboard.getBoolean("ZZ_Template/Sumpthin", sumpthin.get());

        // Put other stuff to be displayed here
        SmartDashboard.putNumber("Snorfler/AC/state", state);
        // SmartDashboard.putString("Snorfler/AC/team color", teamColor);
        // SmartDashboard.putString("Snorfler/AC/enemy color", enemyColor);
        //Motor & SV
        SmartDashboard.putBoolean("Snorfler/AC/Snorfler", IO.snorflerExt_SV.get());
        SmartDashboard.putNumber("Snorfler/AC/SnorfMotor", IO.snorfFeed_Mtr.get());
        SmartDashboard.putNumber("Snorfler/AC/ElevatorMotor", IO.snorfElv_Mtrs.get());

        // SmartDashboard.putNumber("Snorfler/Clr/Red", detectedColor.red);
        // SmartDashboard.putNumber("Snorfler/Clr/Green", detectedColor.green);
        // SmartDashboard.putNumber("Snorfler/Clr/Blue", detectedColor.blue);
        // SmartDashboard.putString("Snorfler/Clr/Detected Color", colorString);
        
        //Joysticks
        SmartDashboard.putBoolean("Snorfler/JS/btnSnorfle", btnSnorfle.isDown());
        SmartDashboard.putBoolean("Snorfler/JS/btnRejectSnorfle", btnRejectSnorfle.isDown());
        SmartDashboard.putBoolean("Snorfler/JS/btnBadColor", btnBadColor.isDown());
        SmartDashboard.putBoolean("Snorfler/JS/btnRejectSnorfleUp", btnRejectSnorfle.isUp());
        SmartDashboard.putBoolean("Snorfler/JS/btnRejectSnorfleDown", btnRejectSnorfle.isDown());
    }

    // ----------------- Shooter statuses and misc.-----------------

    private static void ballColorUpdate(){
        teamColor = teamColorchsr.getSelected();      //Driver choosen team color
        enemyColor = teamColor == "Blue" ? "Red" : 
                     teamColor == "Red" ? "Blue" : "none";  //The bad guy's color.
        
        detectedColor = ballColorSensor.getColor();
        match = colorMatcher.matchClosestColor(detectedColor);

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

    }
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