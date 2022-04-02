package frc.util;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.math.MathUtil;

public class PIDXController extends PIDController{

    private double kInFB = 0.0;     //Abs value feedback (for troubleshooting)
    private double kInDB = 0.0;     //Abs value around SP that kOut is 0.0
    private double kOutMn = 0.0;    //Rescale kOut to hold 0 as this.
    private double kOutMx = 1.0;    //Rescale kOut to hold 1.0 as this.
    private double kOutExp = 1.0;   //Exponent: 1=Linear, 2=Sqrd, 3=Cubed
    private double kFF = 0.0;       //PID feed forward, -1.0 to 1.0
    private double kOut = 0.0;      //Output of PIDController.
    private double kAdj = 0.0;      //Output after adjustments. Returned value.
    private boolean KClamp = false;  //Limit kAdj to +/- kOutMx

    //Constructor
    public PIDXController(double kp, double ki, double kd) {
        super(kp, ki, kd);
        System.out.println("-----  P: " + getP() + "\tI: " + getI() + "\tD: " + getD());
        setTolerance(0.02); //There is no getTolerance, so I save local kInDB
        // TODO Auto-generated constructor stub
    }

    //Constructor
    public PIDXController() {
        super(0.1, 0.0, 0.0);   
        setTolerance(0.02); //There is no getTolerance, so I save local kInDB
    }

    public double calculateX( double _fb, double _sp){
        kInFB = _fb;    //for troubleshooting
        kOut = super.calculate(_fb, _sp);   //This sets setpoint to _sp
        kAdj = (atSetpoint() ? 0 : calcX(kOut)) + kFF;
        if( KClamp ){ kAdj = MathUtil.clamp(kAdj, -kOutMx, kOutMx); }
        return kAdj;
    }

    public double calculateX( double _fb){
        return calculateX(_fb, getSetpoint()); //This uses existing setpoint
    }

    private double calcX(double pidNum){
        double baseLin = (pidNum + Math.signum(pidNum) * kInDB * getP());
        baseLin /=  (1.0 - kInDB * Math.abs(getP()));
        baseLin = Math.signum(pidNum) *
                (kOutMn + Math.pow(Math.abs(baseLin), kOutExp) * (kOutMx - kOutMn));
        return baseLin;
    }

    /**Set kOutMn value */
    public void setOutMn( double mn){ kOutMn = mn; }
    /**Set kOutMx value */
    public void setOutMx( double mx){ kOutMx = Math.abs(mx); }
    /**Set kOutExp value */
    public void setOutExp( double exp){ kOutExp = Math.abs(exp); }
    /**Set kOutFF value */
    public void setOutFF( double ff){ kFF = ff; }
    /**Set Tolerance & kDB value */
    public void setTolerance( double _kDB){
        kInDB = Math.abs(_kDB);
        super.setTolerance(kInDB);
    }
    /**Set Tolerance & kDB value */
    public void setInDB( double _kDB){ setTolerance(_kDB); }
    /**Set clamp, limit kAdj to +/- kOutMx */
    public void setClamp( boolean clamp ) { KClamp = clamp; }

    /**Set extended values for a PIDXController
     * <p> SP, DB, Mn, Mx, Exp, Clmp
     */
    public static void setExt(PIDXController aPidX, double sp, double db, 
    /*                      */double mn, double mx,double exp, Boolean clmp){
        aPidX.setSetpoint(sp);
        aPidX.setInDB(db);
        aPidX.setOutMn(mn);
        aPidX.setOutMx(mx);
        aPidX.setOutExp(exp);
        aPidX.setClamp(clmp);
    }

    /**Set extended values for a PIDXController
     * <p> SP, P (opt), DB, Mn, Mx, Exp, Clmp
     */
    public static void setExt(PIDXController aPidX, double sp, double pb, double db, 
    /*                      */double mn, double mx,double exp, Boolean clmp){
        aPidX.setSetpoint(sp);
        aPidX.setP(pb);
        aPidX.setInDB(db);
        aPidX.setOutMn(mn);
        aPidX.setOutMx(mx);
        aPidX.setOutExp(exp);
        aPidX.setClamp(clmp);
    }


    /** @return kOutMn value */
    public double getOutMn(){ return kOutMn; }
    /** @return kOutMx value */
    public double getOutMx(){ return kOutMx; }
    /** @return kOutExp value */
    public double getOutExp(){ return kOutExp; }
    /** @return kOutFF value */
    public double getOutFF(){ return kFF; }
    /** @return Tolerance,kInDB, value */
    public double getTolerance(){ return kInDB; }
    /** @return kInDB, Tolerance, value */
    public double getInDB(){ return kInDB; }
    /** @return kOut, Output of the PID value */
    public double getOut(){ return kOut; }
    /** @return kAdj, Output of the adjusted PID value, PIDX */
    public double getAdj(){ return kAdj; }
    /** @return kClamp, Limit kAdj to +/- OutMx */
    public boolean getClamp(){ return KClamp; }
    /** @return kInFB, feedback for this pid controller */
    public double getInFB(){ return kInFB; }

    /**Initialize SDB for a PIDXController.  Note groups limited to 10 */
    public static void initSDBPid(PIDXController pidCtlr, String pidTag) {
        SmartDashboard.putNumber("Drv/" + pidTag +"/1_P", pidCtlr.getP());
        SmartDashboard.putNumber("Drv/" + pidTag +"/2_I", pidCtlr.getI());
        SmartDashboard.putNumber("Drv/" + pidTag +"/3_D", pidCtlr.getD());
        SmartDashboard.putNumber("Drv/" + pidTag +"/4_SP",pidCtlr.getSetpoint());
        SmartDashboard.putNumber("Drv/" + pidTag +"/5_FB", pidCtlr.getInFB());
        SmartDashboard.putNumber("Drv/" + pidTag +"/6_DB", pidCtlr.getInDB());
        SmartDashboard.putNumber("Drv/" + pidTag +"/7_Mn", pidCtlr.getOutMn());
        SmartDashboard.putNumber("Drv/" + pidTag +"/8_Mx", pidCtlr.getOutMx());
        SmartDashboard.putNumber("Drv/" + pidTag +"/9_FF", pidCtlr.getOutFF());
        SmartDashboard.putNumber("Drv/" + pidTag +"/A_Exp",pidCtlr.getOutExp());
    }

    /**Update SDB for a PIDXController.  Note groups limited to 10 */
    public static void updSDBPid(PIDXController pidCtlr, String pidTag) {
        pidCtlr.setP( SmartDashboard.getNumber("Drv/" + pidTag + "/1_P", pidCtlr.getP()));
        pidCtlr.setI( SmartDashboard.getNumber("Drv/" + pidTag + "/2_I", pidCtlr.getI()));
        pidCtlr.setD( SmartDashboard.getNumber("Drv/" + pidTag + "/3_D", pidCtlr.getD()));
        SmartDashboard.putNumber("Drv/" + pidTag + "/4_SP", pidCtlr.getSetpoint());
        SmartDashboard.putNumber("Drv/" + pidTag + "/5_FB", pidCtlr.getInFB());
        pidCtlr.setInDB( SmartDashboard.getNumber("Drv/" + pidTag +  "/6_DB", pidCtlr.getInDB()));
        pidCtlr.setOutMn( SmartDashboard.getNumber("Drv/" + pidTag + "/7_Mn", pidCtlr.getOutMn()));
        pidCtlr.setOutMx( SmartDashboard.getNumber("Drv/" + pidTag + "/8_Mx", pidCtlr.getOutMx()));
        pidCtlr.setOutFF( SmartDashboard.getNumber("Drv/" + pidTag + "/9_FF", pidCtlr.getOutFF()));
        pidCtlr.setOutExp( SmartDashboard.getNumber("Drv/" + pidTag +"/A_Exp", pidCtlr.getOutExp()));
    }

}
