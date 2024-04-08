package src.entity;

import src.entity.Utils.MoleculeType;
import src.entity.field.DecomposerField;
import src.entity.field.SignalField;

public class Molecule {
    private final MoleculeType moleculeType;
    private SignalField signalField;
    private DecomposerField decomposerField;

    public Molecule(MoleculeType moleculeType) {
        this.moleculeType = moleculeType;
    }

    public void setSignalField(SignalField signalField) {
        this.signalField = signalField;
    }

    public void setDecomposerField(DecomposerField decomposerField) {
        this.decomposerField = decomposerField;
    }

    public MoleculeType getMoleculeType() {
        return this.moleculeType;
    }

    public SignalField getSignalField() {
        return this.signalField;
    }

    public DecomposerField getDecomposerField() {
        return this.decomposerField;
    }
}
