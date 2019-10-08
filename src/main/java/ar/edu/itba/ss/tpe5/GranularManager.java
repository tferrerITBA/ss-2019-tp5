package ar.edu.itba.ss.tpe5;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.ss.tpe5.Configuration;
import ar.edu.itba.ss.tpe5.Particle;

public class GranularManager {
	
    private final Grid grid;
    private final double timeStep;
    
    public GranularManager(final Grid grid) {
    	this.grid = grid;
    	this.timeStep = Configuration.getTimeStep();
    }
    
    public void execute() {
		List<Particle> previousParticles = initPreviousParticles(grid.getParticles());
		double accumulatedTime = 0.0;
		System.out.println("TIME STEP " + timeStep);
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
			System.out.println("ENTRA AL CICLO");
			Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
			accumulatedTime += timeStep;
			verletUpdate(previousParticles);
			grid.updateGridSections();
			grid.calculateAllParticlesNeighbors();
		}
	}

    public void verletUpdate(List<Particle> previousParticles) {
        List<Particle> currentParticles = grid.getParticles();

        for(int i = 0; i < currentParticles.size(); i++) {
            Particle currParticle = currentParticles.get(i);
            Particle prevParticle = previousParticles.get(i);
            
            Point2D.Double acceleration = getAcceleration(currParticle);
            System.out.println("ACCELERATION " + acceleration);
            double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
                    + Math.pow(timeStep, 2) * acceleration.getX();
            double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * timeStep);
            
            double newPositionY = 2 * currParticle.getPosition().getY() - prevParticle.getPosition().getY()
                    + Math.pow(timeStep, 2) * acceleration.getY();
            double newVelocityY = (newPositionY - prevParticle.getPosition().getY()) / (2 * timeStep);

            prevParticle.setPosition(currParticle.getPosition().getX(), currParticle.getPosition().getY());
            prevParticle.setVelocity(currParticle.getVelocity().getX(), currParticle.getVelocity().getY());
            currParticle.setPosition(newPositionX, newPositionY);
            currParticle.setVelocity(newVelocityX, newVelocityY);
        }
    }

    private Point2D.Double getAcceleration(final Particle p) {
    	Point2D.Double force = getParticleForce(p);
        return new Point2D.Double(force.getX() / p.getMass(), force.getY() / p.getMass());
    }

//    private double getAcceleration(final double position, final double velocity, final double mass) {
//        return getParticleForce(position, velocity) / mass;
//    }

    private Point2D.Double getParticleForce(final Particle p) {
    	double resultantForceX = 0;
    	double resultantForceY = 0;
        for(Particle n : p.getNeighbors()) {
        	double normalUnitVectorX = (n.getPosition().getX() - p.getPosition().getX()) / Math.abs(n.getRadius() - p.getRadius());
        	double normalUnitVectorY = (n.getPosition().getY() - p.getPosition().getY()) / Math.abs(n.getRadius() - p.getRadius());
        	Point2D.Double normalUnitVector = new Point2D.Double(normalUnitVectorX, normalUnitVectorY);
        	Point2D.Double tangentUnitVector = new Point2D.Double(- normalUnitVectorY, normalUnitVectorX);
        	
        	double overlap = p.getRadius() + n.getRadius() - p.getCenterToCenterDistance(n);
        	if(overlap < 0)
        		overlap = 0; // ARREGLAR
        	Point2D.Double relativeVelocity = p.getRelativeVelocity(n);
        	
        	double normalForce = - Configuration.K_NORM * overlap;
        	double tangentForce = - Configuration.K_TANG * overlap * (relativeVelocity.getX() * tangentUnitVector.getX()
        			+ relativeVelocity.getY() * tangentUnitVector.getY());
        	
        	resultantForceX += normalForce * normalUnitVector.getX() + tangentForce * (- normalUnitVector.getY());
        	resultantForceY += normalForce * normalUnitVector.getY() + tangentForce * normalUnitVector.getX();
        }
        return new Point2D.Double(resultantForceX, resultantForceY);
    }

//    private double getParticleForce(final double position, final double velocity) {
//        return - /*Configuration.OSCILLATOR_K*/1 * position - /*Configuration.OSCILLATOR_GAMMA*/1 * velocity;
//    }
    
    // Euler Algorithm evaluated in (- timeStep)
    private List<Particle> initPreviousParticles(final List<Particle> currentParticles) {
    	List<Particle> previousParticles = new ArrayList<>();
		for(Particle p : currentParticles) {
			Particle prevParticle = p.clone();
			Point2D.Double force = getParticleForce(p);
			
			double prevPositionX = p.getPosition().getX() - timeStep * p.getVelocity().getX()
					+ Math.pow(timeStep, 2) * force.getX() / (2 * p.getMass());
			double prevPositionY = p.getPosition().getY() - timeStep * p.getVelocity().getY()
					+ Math.pow(timeStep, 2) * force.getY() / (2 * p.getMass());
			
			double prevVelocityX = p.getVelocity().getX() - (timeStep / p.getMass()) * force.getX();
			double prevVelocityY = p.getVelocity().getY() - (timeStep / p.getMass()) * force.getY();
			
			prevParticle.setPosition(prevPositionX, prevPositionY);
			prevParticle.setVelocity(prevVelocityX, prevVelocityY);
			previousParticles.add(prevParticle);
		}
		
		return previousParticles;
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
