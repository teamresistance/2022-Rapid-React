package frc.io.hdw_io.util;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.util.Units;

public class Encoder_Neo {

    private double tpf;
    private RelativeEncoder neoEnc;

    /**Interface to Spark Max controller with NEO motor encoders */
    public Encoder_Neo(CANSparkMax escPort, double _tpf){
        tpf = _tpf;
        neoEnc = escPort.getEncoder();
    }

    /**@return Encoder ticks. */
    public double ticks(){
        return neoEnc.getPosition();
    }

    /**@return calculated feet from ticks. */
    public double feet(){
        return tpf == 0.0 ? 0.0 : neoEnc.getPosition() / tpf;
    }

    /**@return calcuate meters from feet. */
    public double meters() {
        return Units.feetToMeters(feet());
    }

    /** Reset encoder count to zero. */
    public void reset(){ neoEnc.setPosition(0.0); }

    /**@return the existiing ticks per foot, tpf. */
    public double getTPF() { return tpf; }

    /**@param tpf - Set ticks per foot.  */
    public void setTPF( double tpf) { this.tpf = tpf; }
}
