package frc.robot.testing;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.robot.subsystem.drive.Drive;

public class ClimbTest {
    public static void init(){
    }

    public static void update(){
        //IO.climbBrakeRel_SV.set(JS_IO.btnFire.isDown());  //TEST BRAKE FIRST.
        // KEEP BRAKE RELEASED WHEN TESTING MOTORS.  NO SEQUENCING HERE.
        IO.climbMotor.set(-JS_IO.axCoDrvY.get());
         //IO.climbMotorFollow.set(-JS_IO.axCoDrvY.get()); //!!! Disable follower for 1st test !!!

        IO.lockPinAExt_SV.set(JS_IO.btnClimb1.isDown());    //CB 11
        IO.lockPinARet_SV.set(JS_IO.btnClimb2.isDown());    //CB 12
        IO.lockPinBExt_SV.set(JS_IO.btnRejectLeft.isDown());//CB 4
        IO.sliderExt_SV.set(JS_IO.btnRejectRight.isDown()); //CB 6


    
        sdbUpdate();
    }

    private static void sdbUpdate(){
        SmartDashboard.putBoolean("Test/Climb/AExt_L_FB", IO.lockPinAExt_L_FB.get());
        SmartDashboard.putBoolean("Test/Climb/AExt_R_FB", IO.lockPinAExt_R_FB.get());
        SmartDashboard.putBoolean("Test/Climb/BExt_L_FB", IO.lockPinBExt_L_FB.get());
        SmartDashboard.putBoolean("Test/Climb/BRet_R_FB", IO.lockPinBExt_R_FB.get());
        SmartDashboard.putBoolean("Test/Climb/SExt_L_FB", IO.sliderExt_L_FB.get());
        SmartDashboard.putBoolean("Test/Climb/SExt_R_FB", IO.sliderExt_R_FB.get());

        SmartDashboard.putNumber("Test/Climb/Motor6_cmd", IO.climbMotor.get());
        SmartDashboard.putNumber("Test/Climb/Motor6_axis", -JS_IO.axCoDrvY.get());
        SmartDashboard.putNumber("Test/Climb/Motor7_cmd", IO.climbMotorFollow.get());
        SmartDashboard.putNumber("Test/Climb/Motor7_volt", IO.climbMotorFollow.getBusVoltage());
        SmartDashboard.putNumber("Test/Climb/Motor6_volt", IO.climbMotor.getBusVoltage());

        SmartDashboard.putNumber("Test/Climb/JS/Feet",   Drive.distFB());
        SmartDashboard.putNumber("Test/Climb/JS/Lead Mtr",   JS_IO.axLeftY.get());
        SmartDashboard.putNumber("Test/Climb/JS/Follow Mtr", JS_IO.axRightY.get());
        SmartDashboard.putBoolean("Test/Climb/JS/Pin A Ext", JS_IO.btnClimb1.isDown());
        SmartDashboard.putBoolean("Test/Climb/JS/Pin A Ret", JS_IO.btnClimb2.isDown());
        SmartDashboard.putBoolean("Test/Climb/JS/Pin B Ext", JS_IO.btnRstGyro.isDown());
        SmartDashboard.putBoolean("Test/Climb/JS/Slide Ext", JS_IO.btnRstFeet.isDown());

    }
    
}
