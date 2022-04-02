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

        IO.lockPinAExt_SV.set(JS_IO.btnFire.isDown());      //CB 1 / GP 6
        IO.lockPinARet_SV.set(JS_IO.btnSnorfle.isDown());   //CB 3 / GP 5
        IO.lockPinBExt_SV.set(JS_IO.btnRejectLeft.isDown());//CB 4 / GP 3
        IO.sliderExt_SV.set(JS_IO.btnRejectRight.isDown()); //CB 6 / GP 4
        IO.climbBrakeRel_SV.set(JS_IO.btnRst.isDown());     //LD 7 / GP 1
    
        sdbUpdate();
    }

    /**
     * RET - both retracted
     * <p>LEFT - Left extended, Right retracted
     * <p>RIGHT - Left retracted, Right extended
     * <p>EXT - both extended
     */
    private static enum eStsFB {RET, LEFT, RIGHT, EXT};

    /**
     * 
     * @param in1 - Left extended feedback
     * @param in2 - Right extended feedback
     * @return status of combined Left & Right extended feedbacks.
     * <p>RET - both retracted
     * <p>LEFT - Left extended, Right retracted
     * <p>RIGHT - Left retracted, Right extended
     * <p>EXT - both extended
     */
    private static eStsFB e2DIStatus(boolean in1, boolean in2){
        int tmp = in1 ? 1 : 0;
        tmp += in2 ? 2 : 0;
        switch(tmp){
            case 0: return eStsFB.RET;
            case 3: return eStsFB.EXT;
            case 1: return eStsFB.LEFT;
            default: return eStsFB.RIGHT;
        }
    }

    /**Combined status of left & right locking pins A. */
    private static eStsFB lockPinA_Sts(){
        return e2DIStatus(IO.lockPinAExt_L_FB.get(), IO.lockPinAExt_R_FB.get());
    }

    /**Combined status of left & right locking pins B. */
    private static eStsFB lockPinB_Sts(){
        return e2DIStatus(IO.lockPinBExt_L_FB.get(), IO.lockPinBExt_R_FB.get());
    }

    /**Combined status of left & right slider feedbacks. */
    private static eStsFB slider_Sts(){
        return e2DIStatus(IO.sliderExt_L_FB.get(), IO.sliderExt_R_FB.get());
    }

    private static void sdbUpdate(){
        SmartDashboard.putBoolean("Test/Climb/AExt_L_FB", IO.lockPinAExt_L_FB.get());
        SmartDashboard.putBoolean("Test/Climb/AExt_R_FB", IO.lockPinAExt_R_FB.get());
        SmartDashboard.putBoolean("Test/Climb/BExt_L_FB", IO.lockPinBExt_L_FB.get());
        SmartDashboard.putBoolean("Test/Climb/BRet_R_FB", IO.lockPinBExt_R_FB.get());
        SmartDashboard.putBoolean("Test/Climb/SExt_L_FB", IO.sliderExt_L_FB.get());
        SmartDashboard.putBoolean("Test/Climb/SExt_R_FB", IO.sliderExt_R_FB.get());
        SmartDashboard.putString("Test/Climb/lockPinA Sts, L && B", lockPinA_Sts().toString());
        SmartDashboard.putString("Test/Climb/lockPinB Sts, L && B", lockPinB_Sts().toString());
        SmartDashboard.putString("Test/Climb/SExt Sts, L && B", slider_Sts().toString());

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
    }
    
}
