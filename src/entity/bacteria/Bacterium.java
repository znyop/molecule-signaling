package src.entity.bacteria;

import javax.vecmath.Vector3d;

import src.bsim.BSim;
import src.bsim.particle.BSimParticle;

public abstract class Bacterium extends BSimParticle {
    public Bacterium(BSim sim, Vector3d position, double radius) {
        super(sim, position, radius);
    }

    public abstract void vesiculateSignal(int quantity);

    public abstract void vesiculateDecomposer(int quantity);

    public abstract void action();
}
