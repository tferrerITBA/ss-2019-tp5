package ar.edu.itba.ss.tpe5;

import java.util.List;
import java.util.Random;

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
    
//	public void executeOffLatice() {
//	for(int i = 0; i < Configuration.getTimeLimit(); i++) {
//		List<Particle> updatedParticles = new ArrayList<>(Configuration.getParticleCount());
//		calculateAllParticlesNeighbors();
//		if(Configuration.isSingleRunMode())
//			Configuration.writeOvitoOutputFile(i, particles);
//		updateParticles(updatedParticles);
//		setParticles(updatedParticles);
//		updateGridSections();
//	}
//}
    
//	private void updateParticles(List<Particle> updatedParticles) {
//		for(Particle p : particles) {
//			Particle updatedParticle = p.clone();
//			double newPositionX = p.getPosition().getX() + p.getVelocity().getX() * 1;
//			if(newPositionX < 0 || newPositionX > areaBorderLength)
//				newPositionX = (newPositionX + areaBorderLength) % areaBorderLength;
//			double newPositionY = p.getPosition().getY() + p.getVelocity().getY() * 1;
//			if(newPositionY < 0 || newPositionY > areaBorderLength)
//				newPositionY = (newPositionY + areaBorderLength) % areaBorderLength;
//			updatedParticle.setPosition(newPositionX, newPositionY);
//			
//			double accumVelocityX = p.getVelocity().getX();
//			double accumVelocityY = p.getVelocity().getY();
//			for(Particle n : p.getNeighbors()) {
//				accumVelocityX += n.getVelocity().getX();
//				accumVelocityY += n.getVelocity().getY();
//			}
//			double eta = Configuration.getEta();
//			Random r = new Random();
//			double newAngle = Math.atan2(accumVelocityY / (p.getNeighbors().size() + 1), accumVelocityX / (p.getNeighbors().size() + 1))
//					+ (-eta/2 + r.nextDouble() * eta);
//			double newVelocityX = Math.cos(newAngle) * Configuration.getVelocity();
//			double newVelocityY = Math.sin(newAngle) * Configuration.getVelocity();
//			updatedParticle.setVelocity(newVelocityX, newVelocityY);
//			
//			updatedParticles.add(updatedParticle);
//		}
//	}
}
