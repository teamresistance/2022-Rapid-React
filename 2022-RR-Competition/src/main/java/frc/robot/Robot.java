// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.robot.subsystem.Climber;
import frc.robot.subsystem.Shooter;
import frc.robot.subsystem.Snorfler;
import frc.robot.subsystem.Test_Hdw;
import frc.robot.subsystem.drive.Drv_Teleop;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

    /**
     * This function is run when the robot is first started up and should be used
     * for any
     * initialization code.
     */
    @Override
    public void robotInit() {
      IO.init();
      JS_IO.init();
    }

    /**
     * This function is called every robot packet, no matter the mode. Use this for
     * items like
     * diagnostics that you want ran during disabled, autonomous, teleoperated and
     * test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and
     * SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        IO.update();
        JS_IO.update();
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
        Test_Hdw.init();
        Drv_Teleop.init();
        Snorfler.init();
        Shooter.init();
        Climber.init();
    }

    /** This function is called periodically during operator control. */
    @Override
    public void teleopPeriodic() {
        Test_Hdw.update();
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
    }

    /** This function is called periodically during test mode. */
    @Override
    public void testPeriodic() {
    }
}
