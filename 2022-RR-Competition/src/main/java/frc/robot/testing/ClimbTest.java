package frc.robot.testing;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Button;
import frc.robot.subsystem.drive.Drive;

public class ClimbTest {
    private static Button tglPinA_Btn = JS_IO.btnSnorfle;          //CB 3 / GP 5
    private static Button tglPinB_Btn = JS_IO.btnRejectLeft;       //CB 4 / GP 3
    private static Button tglSlider_Btn = JS_IO.btnRejectSnorfle;  //CB 5 / GP 1
    private static Button tglBrake_Btn = JS_IO.btnRejectRight;     //CB 6 / GP 4

    private static boolean pinAExt = true;          //Control pin A extend
    private static boolean pinBExt = true;          //Control pin B extend
    private static boolean sliderExt = true;        //Control slider extend
    private static boolean manBrakeRel = false;     //Control manual brake release
    private static boolean mtrBrakeRel = false;     //Control motor brake release

    public static void init(){
        pinAExt = true;
        pinBExt = true;
        sliderExt = true;
        manBrakeRel = false;
        mtrBrakeRel = false;
    }

    public static void update(){
        if(tglPinA_Btn.onButtonPressed()) {pinAExt = !pinAExt;}
        if(tglPinB_Btn.onButtonPressed()) {pinBExt = !pinBExt;}
        if(tglSlider_Btn.onButtonPressed()) {sliderExt = !sliderExt;}
        if(tglBrake_Btn.onButtonPressed()) {manBrakeRel = !manBrakeRel;}

        //IO.climbBrakeRel_SV.set(JS_IO.btnFire.isDown());  //TEST BRAKE FIRST.
        // KEEP BRAKE RELEASED WHEN TESTING MOTORS.  NO SEQUENCING HERE.
        IO.climbMotor.set(-JS_IO.axCoDrvY.get());
        mtrBrakeRel = Math.abs(IO.climbMotor.get()) > 0.0;  //CAUTION: Simple brake release
         //IO.climbMotorFollow.set(-JS_IO.axCoDrvY.get()); //!!! Disable follower for 1st test !!!

        IO.lockPinAExt_SV.set(pinAExt);     //CB 3 / GP 5
        IO.lockPinARet_SV.set(!pinAExt);    //CB 3 / GP 5
        IO.lockPinBExt_SV.set(pinBExt);     //CB 4 / GP 3
        IO.sliderExt_SV.set(sliderExt);     //CB 5 / GP 1
        IO.climbBrakeRel_SV.set(manBrakeRel || mtrBrakeRel);  //CB 6 / GP 4 (man)
    
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
        SmartDashboard.putBoolean("Test/Climb/Mtr Brake Rel", IO.climbBrakeRel_SV.get());

        SmartDashboard.putNumber("Test/Climb/JS/Lead Mtr",   JS_IO.axLeftY.get());      //CAUTION: left only do not use with right
        SmartDashboard.putNumber("Test/Climb/JS/Follow Mtr", JS_IO.axRightY.get());     //CAUTION: only if set as not follower
        SmartDashboard.putBoolean("Test/Climb/JS/Toggle Pin A Ext", JS_IO.btnSnorfle.isDown());             //CB 3 / GP 5
        SmartDashboard.putBoolean("Test/Climb/JS/Toggle Pin B Ext", JS_IO.btnRejectLeft.isDown());          //CB 4 / GP 3
        SmartDashboard.putBoolean("Test/Climb/JS/Toggle Slider Ext", JS_IO.btnRejectSnorfle.isDown());      //CB 5 / GP 1
        SmartDashboard.putBoolean("Test/Climb/JS/Toggle Manual Brake Rel", JS_IO.btnRejectRight.isDown());  //CB 6 / GP 4
        SmartDashboard.putBoolean("Test/Climb/JS/Motor Brake Rel", JS_IO.btnRejectRight.isDown());  //CB 6 / GP 4

    }
    
}
