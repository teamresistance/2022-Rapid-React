// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

/*
!!! --- Preserve the code in Main.  DO NOT CHANGE MAIN ---!!!
This branch is for code changes after the Orlando Regional.
*/

package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.util.CoorSys;
import frc.io.joysticks.JS_IO;
import frc.robot.subsystem.Climber;
import frc.robot.subsystem.Shooter;
import frc.robot.subsystem.Snorfler;
import frc.robot.subsystem.Test_Hdw;
import frc.robot.subsystem.drive.Drive;
import frc.robot.subsystem.drive.Drv_Auto;
import frc.robot.subsystem.drive.Drv_Teleop;
import frc.robot.subsystem.drive.Trajectories;
import frc.robot.subsystem.driveSimple.AutoDrv00;
import frc.robot.subsystem.driveSimple.AutoDrv01;
import frc.robot.subsystem.driveSimple.AutoDrv02;
import frc.robot.subsystem.driveSimple.AutoDrv03;
import frc.robot.subsystem.driveSimple.AutoDrv04;
import frc.robot.subsystem.driveSimple.AutoDrv05;
import edu.wpi.first.wpilibj.Relay;

import frc.robot.testing.ClimbTest;
import frc.robot.testing.DriveTest;
import frc.robot.testing.ShootTest;
import frc.robot.testing.SnorfTest;
import frc.robot.testing.TimerTest;
//TODO: check class placement
// import edu.wpi.first.cameraserver.CameraServer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the project.
 */
public class Robot extends TimedRobot {

    private static boolean cmprEna = true; // Don't need cmpr when testing drive.
    private static SendableChooser<String> chsr = new SendableChooser<String>();
    private static String[] chsrDesc = {
        "AutoDrv00", "AutoDrv01", "AutoDrv02", "AutoDrv03", "AutoDrv04", "Pick up ball",
    };
    /**Initialize Traj chooser */
    public static void chsrInit(){
        for(int i = 0; i < chsrDesc.length; i++){
            chsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        chsr.setDefaultOption(chsrDesc[0] + " (Default)", chsrDesc[0]);   //Default MUST have a different name
        SmartDashboard.putData("Drv/Auto/Choice", chsr);
        
    }

    /**Show on sdb traj chooser info.  Called from robotPeriodic  */
    public static void chsrUpdate(){
        SmartDashboard.putString("Drv/Auto/Choosen", chsr.getSelected());
    }

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
        IO.init();
        JS_IO.init();

        Snorfler.teamColorchsrInit();
        Drv_Teleop.chsrInit(); // Drv_Teleop init Drv type Chooser.
        Trajectories.chsrInit();
        chsrInit();

        SmartDashboard.putBoolean("Robot/Cmpr Enabled", cmprEna);
        // CameraServer.startAutomaticCapture();
    }

    /**
     * This function is called every robot packet, no matter the mode. Use this for
     * items like diagnostics that you want ran during disabled, autonomous,
     * teleoperated and test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        // Pneu. system has leak. Dont need it when testing drive.
        cmprEna = SmartDashboard.getBoolean("Robot/Cmpr Enabled", cmprEna);
        IO.compressorRelay.set(IO.compressor1.enabled() && cmprEna ? Relay.Value.kForward : Relay.Value.kOff);

        Snorfler.teamColorchsrUpdate();
        IO.update();
        JS_IO.update();
        Drv_Teleop.chsrUpdate();
        Trajectories.chsrUpdate();
        IO.coorXY.update();
        chsrUpdate();
    }

    /** This function is called once when autonomous is enabled. */

    @Override
    public void autonomousInit() {
        Drive.init();
        Snorfler.init();
        Shooter.init();        
        switch(chsr.getSelected()){
            case "AutoDrv00":
            System.out.println("Auto00");
            AutoDrv00.init();
            break;
            case "AutoDrv01":
            AutoDrv01.init();
            break;
            case "AutoDrv02":
            AutoDrv02.init();
            break;
            case "AutoDrv03":
            AutoDrv03.init();
            break;
            case "AutoDrv04":
            AutoDrv04.init();
            break;
            case "Pick up ball":
            AutoDrv05.init();
            default:
            System.out.println("Robot/Bad Auto " + chsr.getSelected());
            //AutoDrv00.init();
            break;
        }
        // Drv_Auto.init();
        // AutoDrv01.init();
        // AutoDrv03.init();
    }

    /** This function is called periodically during autonomous. */
    @Override
    public void autonomousPeriodic() {
        Snorfler.update();
        Shooter.update();
        Drive.update();
        switch(chsr.getSelected()){
            case "AutoDrv00":
            AutoDrv00.update();
            break;
            case "AutoDrv01":
            AutoDrv01.update();
            break;
            case "AutoDrv02":
            AutoDrv02.update();
            break;
            case "AutoDrv03":
            AutoDrv03.update();
            break;
            case "AutoDrv04":
            AutoDrv04.update();
            break;
            case "Pick up ball":
            AutoDrv05.update();
            default:
            System.out.println("Robot/Bad Auto " + chsr.getSelected());
           // AutoDrv00.update();
        }
        // Drv_Auto.update();
        //AutoDrv01.update();
        // AutoDrv03.update();
    }

    /** This function is called once when teleop is enabled. */
    @Override
    public void teleopInit() {
        Drv_Auto.disable();
        Drive.init();
        Drv_Teleop.init();
        Snorfler.init();
        Shooter.init();
        Climber.init();
    }

    /** This function is called periodically during operator control. */
    @Override
    public void teleopPeriodic() {
        //IO.diffDrv_M.tankDrive(-JS_IO.axLeftY.get(), -JS_IO.axRightY.get());
        //System.out.println("Periodic: " + JS_IO.axLeftY.get() + " - " + JS_IO.axRightY.get());
        Drv_Teleop.update();
        // IO.drvLead_L.set(-JS_IO.axLeftY.get());
        // IO.drvLead_R.set(-JS_IO.axRightY.get());

        Climber.update();
        Snorfler.update();
        Shooter.update();
    }
    /** This function is called once when the robot is disabled. */
    @Override
    public void disabledInit() {
    }

    /** This function is called periodically when disabled. */
    @Override
    public void disabledPeriodic() {
    }

    /** This function is called once when test mode is enabled. */
    @Override
    public void testInit() {
        // ClimbTest.init();
        // SnorfTest.init();
        TimerTest.init();
    }

    /** This function is called periodically during test mode. */
    @Override
    public void testPeriodic() {
        // Test to checkout individual devices. Run one at a tme.
        // SnorfTest.update();
        // ShootTest.update();
        DriveTest.update();
        // ClimbTest.update();
        // TimerTest.update();

    }

}
