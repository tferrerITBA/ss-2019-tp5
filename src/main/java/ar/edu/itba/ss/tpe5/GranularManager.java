package ar.edu.itba.ss.tpe5;

import java.util.List;

public class GranularManager {
    private static Grid grid;

    public void verletUpdate(List<Particle> previousParticles) {
        List<Particle> currentParticles = grid.getParticles();

        for(int i = 0; i < currentParticles.size(); i++) {
            Particle currParticle = currentParticles.get(i);
            Particle prevParticle = previousParticles.get(i);

            double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
                    + Math.pow(Configuration.getTimeStep(), 2) * getAcceleration(currParticle); //+error
            double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * Configuration.getTimeStep()); // + error

            prevParticle.setPosition(currParticle.getPosition().getX(), 0);
            prevParticle.setVelocity(currParticle.getVelocity().getX(), 0);
            currParticle.setPosition(newPositionX, 0);
            currParticle.setVelocity(newVelocityX, 0);
        }
    }

    private double getAcceleration(final Particle p) {
        return getParticleForce(p) / p.getMass();
    }

    private double getAcceleration(final double position, final double velocity, final double mass) {
        return getParticleForce(position, velocity) / mass;
    }

    private double getParticleForce(final Particle p) {
        return - /*Configuration.OSCILLATOR_K*/1 * p.getPosition().getX() - /*Configuration.OSCILLATOR_GAMMA*/1 * p.getVelocity().getX();
    }

    private double getParticleForce(final double position, final double velocity) {
        return - /*Configuration.OSCILLATOR_K*/1 * position - /*Configuration.OSCILLATOR_GAMMA*/1 * velocity;
    }
}
