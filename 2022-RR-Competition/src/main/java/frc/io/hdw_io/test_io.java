package frc.io.hdw_io;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.joysticks.JS_IO;
import com.ctre.phoenix.motorcontrol.ControlMode;

public class test_io{
    private static DifferentialDrive testDiffDrvM = new DifferentialDrive(IO.drvMasterTSRX_L, IO.drvMasterTSRX_R);
    private static DifferentialDrive testDiffDrvS = new DifferentialDrive(IO.drvFollowerVSPX_L, IO.drvFollowerVSPX_R);
    private static boolean drvByDiff = true;

    private static int state = 0;
    private static double tank_L = 0.0;
    private static double tank_R = 0.0;

    // Constructor
    public static void test_io(){
        init();
    }

    // Initializer
    public static void init(){
        sdbInit();
    }

    // I am the determinator
    private static void determ(){
        if(JS_IO.gamePad.getRawButton(1)) state = 0;   //'A' - No testing
        if(JS_IO.gamePad.getRawButton(2)) state = 10;  //'B' - Drive testing
        if(JS_IO.gamePad.getRawButton(3)) state = 20;  //'X' - Shooter, injector, turret, revolver testing
        if(JS_IO.gamePad.getRawButton(4)) state = 30;  //'Y' - Snorfler Testing
        if(JS_IO.gamePad.getRawButton(7)) state = 40;  //'Back' - Climber Testing

        if(JS_IO.gamePad.getRawButtonPressed(5) && state == 10) drvByDiff = true;   //'LB'
        if(JS_IO.gamePad.getRawButtonPressed(6) && state == 10) drvByDiff = false;  //'RB'
    }

    // Update, called by Robot every 20mS.
    public static void update(){
        determ();
        sdbUpdate();
        cmdUpdate();    //added cause getting erro DiffDrv not update often enough.

        switch(state){
        case 0: // All Off.  Turn everything off.  'A'
            tank_L = 0.0;  tank_R = 0.0;
           // IO.shooterTSRX.set(ControlMode.PercentOutput, 0.0);
            IO.injector4Whl.set(ControlMode.PercentOutput, 0.0);
            IO.injectorPickup.set(0.0);
            IO.injectorFlipper.set(false);
            IO.turretRot.set(0.0);
            IO.revolverRot.set(0.0);
            IO.snorflerExt.set(false);
            IO.snorfFeedMain.set(0.0);
            IO.snorfFeedScdy.set(0.0);
            IO.climberExt.set(false);
            IO.climberHoist.set(0.0);
        break;
        case 10:    //Drive Testing.  'B'
            tank_L = -JS_IO.gamePad.getRawAxis(1);
            tank_R = -JS_IO.gamePad.getRawAxis(5);
        break;
        case 20:    //Shooter Testing.  'X'
            //IO.shooterTSRX.set(ControlMode.PercentOutput, Math.abs(JS_IO.gamePad.getRawAxis(1)));
            IO.shooterHoodUp.set(JS_IO.gamePad.getRawButton(5));    //'LB' - Raise hood
            if(JS_IO.gamePad.getRawButton(6)){                      //'RB' - Run Injector
                IO.injector4Whl.set(ControlMode.PercentOutput, 50.0);
                IO.injectorPickup.set(50.0);
                IO.injectorFlipper.set(true);
            }else{
                IO.injector4Whl.set(ControlMode.PercentOutput, 0.0);
                IO.injectorPickup.set(0.0);
                IO.injectorFlipper.set(false);
            }
            IO.turretRot.set(JS_IO.gamePad.getRawAxis(4) / 3);     //'R JS X' - Rotate turret: NO SAFETIES!
            IO.revolverRot.set(JS_IO.gamePad.getRawAxis(2) / 3);   //'LTrgr' - Rotate revolver
            tank_L = 0.0;  tank_R = 0.0;
        break;
        case 30:    // Snorfler testing.  'Y'
            if(JS_IO.gamePad.getRawButton(5)){
                IO.snorflerExt.set(true);
                IO.snorfFeedMain.set(0.75);
                IO.snorfFeedScdy.set(0.75);
            }else{
                IO.snorflerExt.set(false);
                IO.snorfFeedMain.set(0.0);
                IO.snorfFeedScdy.set(0.0);
            }
            tank_L = 0.0;  tank_R = 0.0;
        break;
        case 40:    //Test Climber.  'Back'
            IO.climberExt.set(JS_IO.gamePad.getRawButton(5));   //'LB'
            IO.climberHoist.set(JS_IO.gamePad.getRawAxis(1));   //'L-X'
            tank_L = 0.0;  tank_R = 0.0;
        break;
        case 50:
        break;
        case 60:
        break;
        case 70:
        break;
        case 80:
        break;
        case 90:
        break;
        default:
        }
    }
    
    private static void cmdUpdate(){
        if(drvByDiff){
            testDiffDrvM.tankDrive(tank_L, tank_R);
            testDiffDrvS.tankDrive(tank_L, tank_R);
        }else{
            IO.drvMasterTSRX_L.set(ControlMode.PercentOutput, tank_L);
            IO.drvMasterTSRX_R.set(ControlMode.PercentOutput, tank_R);
        }
    }

    private static void sdbInit(){
        SmartDashboard.putBoolean("Drv Mode", drvByDiff);
    }

    private static void sdbUpdate(){
        SmartDashboard.putNumber("State", state);

        SmartDashboard.putNumber("JSY_L", JS_IO.gamePad.getRawAxis(1));
        SmartDashboard.putNumber("JSX_L", JS_IO.gamePad.getRawAxis(0));
        SmartDashboard.putNumber("JSY_R", JS_IO.gamePad.getRawAxis(5));
        SmartDashboard.putNumber("JSX_R", JS_IO.gamePad.getRawAxis(4));
        SmartDashboard.putNumber("JST_L", JS_IO.gamePad.getRawAxis(2));
        SmartDashboard.putNumber("JST_R", JS_IO.gamePad.getRawAxis(3));

        drvByDiff = SmartDashboard.getBoolean("Drv Mode", drvByDiff);
        SmartDashboard.putNumber("DrvM % L", IO.drvMasterTSRX_L.getMotorOutputPercent());
        SmartDashboard.putNumber("DrvM % R", IO.drvMasterTSRX_L.getMotorOutputPercent());
        SmartDashboard.putNumber("Drv Enc L", IO.drvMasterTSRX_L.getSelectedSensorPosition());
        SmartDashboard.putNumber("Drv Dist L", IO.drvMasterTSRX_L.getSelectedSensorPosition() / 428);
        SmartDashboard.putNumber("Drv Enc R", IO.drvMasterTSRX_R.getSelectedSensorPosition());
        SmartDashboard.putNumber("Drv Dist R", IO.drvMasterTSRX_R.getSelectedSensorPosition() / 428);

        // SmartDashboard.putNumber("Shtr %", IO.shooterTSRX.getMotorOutputPercent());
        // SmartDashboard.putNumber("Shtr Enc", IO.shooterTSRX.getSelectedSensorPosition());
        // SmartDashboard.putNumber("Shtr RPM", IO.shooterTSRX.getSelectedSensorPosition() / 1024.0);

        SmartDashboard.putNumber("Tur %", IO.turretRot.get());
        SmartDashboard.putNumber("Tur Pos", IO.turretPosition.get());
        // SmartDashboard.putNumber("Tur CW LS", IO.turCWLimitSw.get());
        // SmartDashboard.putNumber("Tur CCW LS", IO.turCCWLimitSw.get());

        SmartDashboard.putNumber("Inj 4W %", IO.injector4Whl.getMotorOutputPercent());
        SmartDashboard.putNumber("Inj PU %", IO.injectorPickup.get());
        SmartDashboard.putBoolean("Inj Flip", IO.injectorFlipper.get());

        SmartDashboard.putNumber("Rev %", IO.revolverRot.get());
        SmartDashboard.putBoolean("Rev Idx", IO.revolerIndexer.get());
        SmartDashboard.putBoolean("Rev Nxt Opn", IO.revRcvSlotOpen.get());

        SmartDashboard.putBoolean("Snf Ext", IO.snorflerExt.get());
        SmartDashboard.putNumber("Snf Main %", IO.snorfFeedMain.get());
        SmartDashboard.putNumber("Snf Scdy %", IO.snorfFeedScdy.get());
        SmartDashboard.putBoolean("Snf Has Ball", IO.snorfHasBall.get());
    }

}