package src.entity.controller;

import src.entity.field.SignalField;

public class ReceiverController extends SignalController {
    private SignalField[] input1Fields;
    private SignalField input2Field; // Currently assume input 2 to not have any issue
    private SignalField[] outputFields;

    public ReceiverController(SignalField[] input1Fields, SignalField input2Field, SignalField[] outputFields,
            double lb_threshold) {
        super(lb_threshold);
        assert input1Fields.length == outputFields.length : "Input 1 and Output number of signals must be same";
        this.input1Fields = input1Fields;
        this.input2Field = input2Field;
        this.outputFields = outputFields;
    }

    public SignalField[] getInput1Fields() {
        return this.input1Fields;
    }

    public SignalField getInput2Field() {
        return this.input2Field;
    }

    public SignalField[] getOutputFields() {
        return this.outputFields;
    }

    public SignalField getCurrentInput1Signal() {
        return this.input1Fields[currentSignalIndex];
    }

    public SignalField getCurrentInput2Signal() {
        return this.input2Field;
    }

    public SignalField getCurrentOutputSignal() {
        return this.outputFields[currentSignalIndex];
    }

    public SignalField getNextInput1Signal() {
        return this.input1Fields[(this.currentSignalIndex + 1) % this.input1Fields.length];
    }

    @Override
    public double getConcentration(SignalField signalField) {
        return signalField.getConc(this.bacterium.getPosition());
    }

    @Override
    public ReceiverController changeSignal() {
        this.currentSignalIndex = (this.currentSignalIndex + 1) % this.input1Fields.length;
        return this;
    }

    public void action() {
        if (getConcentration(getNextInput1Signal()) > this.lb_threshold) {
            changeSignal();
        }
    }
}
