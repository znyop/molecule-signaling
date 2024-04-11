package src.entity.controller;

import src.entity.bacteria.Bacterium;
import src.entity.field.SignalField;

public abstract class SignalController {
    protected int currentSignalIndex;
    protected Bacterium bacterium;
    // lower bound threshold, sensitivity to consider concentration as "detect"
    protected double lb_threshold;

    public SignalController(double lb_threshold) {
        this.currentSignalIndex = 0;
        this.lb_threshold = lb_threshold;
    }

    public void setBacterium(Bacterium bacterium) {
        this.bacterium = bacterium;
    }

    public double getLbThreshold() {
        return this.lb_threshold;
    }

    public abstract double getConcentration(SignalField signalField);

    public abstract SignalController changeSignal();
}
