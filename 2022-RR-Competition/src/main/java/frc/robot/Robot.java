// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.robot.subsystem.Climber;
import frc.robot.subsystem.Shooter;
import frc.robot.subsystem.Snorfler;
import frc.robot.subsystem.Test_Hdw;
import frc.robot.subsystem.drive.Drv_Teleop;
import edu.wpi.first.wpilibj.Relay;

import frc.robot.testing.ClimbTest;
import frc.robot.testing.DriveTest;
import frc.robot.testing.ShootTest;
import frc.robot.testing.SnorfTest;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the project.
 */
public class Robot extends TimedRobot {

    private static boolean cmprEna = true; // Don't need cmpr when testing drive.

<<<<<<< HEAD
    public static SendableChooser<String> teamColorchsr = new SendableChooser<String>();
    private static String[] chsrDesc = { "Blue", "Red", "none" };

    /** Initialize Traj chooser */
    public static void teamColorchsrInit() {
        for (int i = 0; i < chsrDesc.length; i++) {
            teamColorchsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        teamColorchsr.setDefaultOption(chsrDesc[0] + " (Default)", chsrDesc[0]); // Default MUST have a different name
        SmartDashboard.putData("Robot/TeamColor", teamColorchsr);
    }

    /** Show on sdb traj chooser info. Called from robotPeriodic */
    public static void teamColorchsrUpdate() {
        SmartDashboard.putString("Robot/TeamColorChoosen", teamColorchsr.getSelected());
    }
=======
    /**
     * This function is run when the robot is first started up and should be used
     * for any
     * initialization code.
     */
>>>>>>> 3ed02fbb853bb13c77a04418c472431dd677f6db

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
<<<<<<< HEAD
        teamColorchsrInit();
        IO.init();
        JS_IO.init();
        Drv_Teleop.chsrInit(); // Drv_Teleop init Drv type Chooser.

        SmartDashboard.putBoolean("Robot/Cmpr Enabled", cmprEna);
=======
      Snorfler.teamColorchsrInit();
      IO.init();
      JS_IO.init();
      Drv_Teleop.chsrInit();      //Drv_Teleop init Drv type Chooser.
>>>>>>> 3ed02fbb853bb13c77a04418c472431dd677f6db
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
    }

    /** This function is called once when autonomous is enabled. */

    @Override
    public void autonomousInit() {

    }

    /** This function is called periodically during autonomous. */
    @Override
    public void autonomousPeriodic() {

    }

    /** This function is called once when teleop is enabled. */
    @Override
    public void teleopInit() {
        Drv_Teleop.init();
        Snorfler.init();
        Shooter.init();
        Climber.init();
    }

    /** This function is called periodically during operator control. */
    @Override
    public void teleopPeriodic() {
        Drv_Teleop.update();
        Snorfler.update();
        Shooter.update();
        Climber.update();
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
        ClimbTest.init();
        // SnorfTest.init();
    }

    /** This function is called periodically during test mode. */
    @Override
    public void testPeriodic() {
        // Test to checkout individual devices. Run one at a tme.
        // SnorfTest.update();
        // ShootTest.update();
        // DriveTest.update();
        ClimbTest.update();

    }

}
