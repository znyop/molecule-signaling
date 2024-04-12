package src.entity.controller;

import src.bsim.BSim;
import src.entity.field.SignalField;

public class TransmitterController extends SignalController {
    private BSim sim;
    private SignalField[] signalFields;
    // true means bit 1, false means bit 0
    private boolean emittingBit;
    // upper bound threshold, sensitivity to consider concentration as "saturated"
    private double ub_threshold;
    private boolean is_on;
    private double rechargedTime;

    public TransmitterController(BSim sim, SignalField[] signalFields, boolean emittingBit, double lb_threshold,
            double ub_threshold) {
        super(lb_threshold);
        this.sim = sim;
        this.signalFields = signalFields;
        this.emittingBit = emittingBit;
        this.ub_threshold = ub_threshold;
        this.is_on = true;
        this.rechargedTime = 0;
    }

    public SignalField[] getSignalFields() {
        return this.signalFields;
    }

    public boolean getEmittingBit() {
        return this.emittingBit;
    }

    public double getUbThreshold() {
        return this.ub_threshold;
    }

    public SignalField getCurrentSignal() {
        return this.signalFields[currentSignalIndex];
    }

    public SignalField getNextSignal() {
        return this.signalFields[(this.currentSignalIndex + 1) % this.signalFields.length];
    }

    @Override
    public double getConcentration(SignalField signalField) {
        double conc = 0;
        if (this.bacterium != null) {
            conc = signalField.getConc(this.bacterium.getPosition());
        } else {
            assert false : "Bacteria is null. No position given";
        }
        return conc;
    }

    @Override
    public TransmitterController changeSignal() {
        this.currentSignalIndex = (this.currentSignalIndex + 1) % this.signalFields.length;
        return this;
    }

    public boolean isActive() {
        // Technically, this.is_on is not required, but this makes quicker short-circuit
        this.is_on = this.is_on || this.sim.getTime() > this.rechargedTime;
        return this.is_on;
    }

    private void turnOff(double seconds) {
        this.is_on = false;
        this.rechargedTime = this.sim.getTime() + seconds;
    }

    public boolean action() {
        // return false if does nothing due to inactive, else true
        if (!this.isActive()) {
            return false;
        }
        if (emittingBit) { // bit=1
            if (getConcentration(getCurrentSignal()) > this.ub_threshold
                    || getConcentration(getNextSignal()) > this.lb_threshold) {
                // First case is when concentration to high, change signal
                // Second case is when interfering next-signal-molecules detected, change to
                // that signal to hit threshold
                changeSignal();
                turnOff(5);
            }
        } else { // bit=0
            if (getConcentration(getCurrentSignal()) > this.lb_threshold
                    && getConcentration(getCurrentSignal()) < this.ub_threshold) {
                // less than ub_threshold because we expect interrupting molecules propagate
                // slowly so it should not have high concentration when detected
                // this also help in avoiding delayed detection and contradiction when neighbors
                // emit a pulse
                changeSignal();
                turnOff(5);
            } else if (getConcentration(getNextSignal()) > this.lb_threshold) {
                if (getConcentration(getNextSignal()) > this.ub_threshold) {
                    // this case is pulse from neighbor
                    changeSignal();
                } else {
                    changeSignal().changeSignal();
                }
                turnOff(5);
            }
        }
        return true;
    }
}
