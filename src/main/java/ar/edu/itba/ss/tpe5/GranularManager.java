package ar.edu.itba.ss.tpe5;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GranularManager {
	
    private final Grid grid;
		private final double timeStep;
		private double accumulatedTime = 0.0;
    
    public GranularManager(final Grid grid) {
    	this.grid = grid;
    	this.timeStep = Configuration.getTimeStep();
    }
    
    public void execute() {
		List<Particle> previousParticles = initPreviousParticles(grid.getParticles());
		double accumulatedPrintingTime = 0.0;
		double printingTimeLimit = 0.005; //s
		System.out.println("TIME STEP " + timeStep);
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
			grid.calculateAllParticlesNeighbors();
			if (accumulatedPrintingTime >= printingTimeLimit) {
				Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
				accumulatedPrintingTime = 0;
			}
			accumulatedTime += timeStep;
			accumulatedPrintingTime += timeStep;
			List<Particle> updatedParticles = new ArrayList<>(previousParticles.size());
			verletUpdate(previousParticles, updatedParticles);

			grid.setParticles(updatedParticles);
			grid.updateGridSections();
		}
	}

    public void verletUpdate(List<Particle> previousParticles, List<Particle> updatedParticles) {
        List<Particle> currentParticles = grid.getParticles();//.stream().map(p -> p.clone()).collect(Collectors.toList());
        List<Particle[]> repositionParticles = new ArrayList<>();

        for(int i = 0; i < currentParticles.size(); i++) {
            Particle currParticle = currentParticles.get(i);
            Particle prevParticle = previousParticles.get(i);
            Particle updatedParticle = currParticle.clone();
            
            Point2D.Double acceleration = getAcceleration(currParticle);
            
            double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
                    + Math.pow(timeStep, 2) * acceleration.getX();
            double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * timeStep);
            
            double newPositionY = 2 * currParticle.getPosition().getY() - prevParticle.getPosition().getY()
                    + Math.pow(timeStep, 2) * acceleration.getY();
            double newVelocityY = (newPositionY - prevParticle.getPosition().getY()) / (2 * timeStep);
            
            if(newPositionY < 0) {
            	repositionParticles.add(new Particle[] {prevParticle, updatedParticle});
//            	Random r = new Random();
//            	Point2D.Double newPosition = getRandomTopPosition(currParticle, );
//            	prevParticle.setPosition(currParticle.getPosition().getX(), Configuration.BOX_HEIGHT + Configuration.MIN_PARTICLE_HEIGHT);
//                prevParticle.setVelocity(0, 0);
//                updatedParticle.setPosition(currParticle.getPosition().getX(), Configuration.BOX_HEIGHT + Configuration.MIN_PARTICLE_HEIGHT);
//            	//currParticle.setPosition(currParticle.getPosition().getX(), Configuration.BOX_HEIGHT + Configuration.MIN_PARTICLE_HEIGHT);
//				updatedParticle.setVelocity(0, 0);
                //currParticle.setVelocity(0, 0);
            } else {
            	prevParticle.setPosition(currParticle.getPosition().getX(), currParticle.getPosition().getY());
                prevParticle.setVelocity(currParticle.getVelocity().getX(), currParticle.getVelocity().getY());
                updatedParticle.setPosition(newPositionX, newPositionY);
                //currParticle.setPosition(newPositionX, newPositionY);
				updatedParticle.setVelocity(newVelocityX, newVelocityY);
                //currParticle.setVelocity(newVelocityX, newVelocityY);
                // if(newPositionY > 1.1)
                // 	System.out.println(currParticle.getId() + " Y " + newPositionY + " ACC " + acceleration + " VEL " + newVelocityY);
                // if(newPositionX > 1)
                // 	System.out.println(currParticle.getId() + " X " + newPositionX + " ACC " + acceleration + " VEL " + newVelocityX);
						}
						updatedParticle.setPressure(currParticle.getPressure());
            updatedParticles.add(updatedParticle);
        }
        for(Particle[] repParticles : repositionParticles) {
        	repositionParticles(repParticles, updatedParticles);
        }
        //grid.setParticles(currentParticles);
    }

    private void repositionParticles(final Particle[] repParticles, final List<Particle> updatedParticles) {
			// write to exit file
			Configuration.writeExitFile(accumulatedTime);

			double randomPositionX = 0;
			double randomPositionY = Configuration.BOX_HEIGHT + Configuration.MIN_PARTICLE_HEIGHT - repParticles[0].getRadius();
			Random r = new Random();
			boolean isValidPosition = false;
			double limit = 100;
			double accumLimit = 0;
			while(!isValidPosition) {
				if (accumLimit == limit) {
					randomPositionY -= repParticles[0].getRadius();
					accumLimit = 0;
				}
				randomPositionX = (Configuration.BOX_WIDTH - 2 * repParticles[0].getRadius()) * r.nextDouble() + repParticles[0].getRadius();
				isValidPosition = Configuration.validateParticlePosition(updatedParticles, randomPositionX, randomPositionY, repParticles[0].getRadius());
				accumLimit++;
			}
			// System.out.println("REPOS " + repParticles[0].getId() + " " + randomPositionX + " " + newPositionY);
			repParticles[0].setPosition(randomPositionX, randomPositionY);
			repParticles[0].setVelocity(0, 0);
			repParticles[1].setPosition(randomPositionX, randomPositionY);
			repParticles[1].setVelocity(0, 0);
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
			double resultantForceN = 0;
			List<Particle> neighbors = new ArrayList<>(p.getNeighbors());
			// Add as neighbors two particles for the corners
			neighbors.add(new Particle(Configuration.MIN_PARTICLE_RADIUS * 0.1, Configuration.PARTICLE_MASS, (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2, Configuration.MIN_PARTICLE_HEIGHT, 0, 0));
			neighbors.add(new Particle(Configuration.MIN_PARTICLE_RADIUS * 0.1, Configuration.PARTICLE_MASS, (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2 + Configuration.HOLE_WIDTH, Configuration.MIN_PARTICLE_HEIGHT, 0, 0));
        for(Particle n : neighbors) {
        	double normalUnitVectorX = (n.getPosition().getX() - p.getPosition().getX()) / Math.abs(n.getRadius() - p.getRadius());
        	double normalUnitVectorY = (n.getPosition().getY() - p.getPosition().getY()) / Math.abs(n.getRadius() - p.getRadius());
        	double norm = Math.sqrt(Math.pow(normalUnitVectorX, 2) + Math.pow(normalUnitVectorY, 2));
        	normalUnitVectorX /= norm;
        	normalUnitVectorY /= norm;
        	Point2D.Double normalUnitVector = new Point2D.Double(normalUnitVectorX, normalUnitVectorY);
        	Point2D.Double tangentUnitVector = new Point2D.Double(- normalUnitVectorY, normalUnitVectorX);
        	
        	double overlap = p.getRadius() + n.getRadius() - p.getCenterToCenterDistance(n);
        	if(overlap < 0)
						overlap = 0; // ARREGLAR
					//if (overlap > 0) System.out.println(overlap);
        	Point2D.Double relativeVelocity = p.getRelativeVelocity(n);
        	
					double normalForce = - Configuration.K_NORM * overlap;
        	double tangentForce = - Configuration.K_TANG * overlap * (relativeVelocity.getX() * tangentUnitVector.getX()
					+ relativeVelocity.getY() * tangentUnitVector.getY());
        	
					resultantForceN += normalForce;
        	resultantForceX += normalForce * normalUnitVector.getX() + tangentForce * (- normalUnitVector.getY());
					resultantForceY += normalForce * normalUnitVector.getY() + tangentForce * normalUnitVector.getX();
					
					//System.out.println("FUERZA X ENTRE PARTS " + resultantForceX);
//					if(resultantForceY > 0) {
//						System.out.println("FUERZA Y ENTRE PARTS " + resultantForceY + " N " + normalForce + " T " + tangentForce + " Unit " + normalUnitVector);
//						System.out.println(n.getPosition().getY() + " " + p.getPosition().getY() + " " + n.getRadius() + " " + p.getRadius());
//        
//					}
        }
				
        // Check for border overlap
        double horizBorderOverlap = 0;
        double boxHoleStartingX = (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2;
				double boxHoleEndingX = boxHoleStartingX + Configuration.HOLE_WIDTH;
				boolean isWithinHole = p.getPosition().getX() > boxHoleStartingX && p.getPosition().getX() < boxHoleEndingX;
        if (!isWithinHole && Math.abs(p.getPosition().getY() - Configuration.MIN_PARTICLE_HEIGHT) < p.getRadius()) {
        	horizBorderOverlap = (p.getRadius() - Math.abs(p.getPosition().getY() - Configuration.MIN_PARTICLE_HEIGHT));
				}

        resultantForceY += Configuration.K_NORM * horizBorderOverlap;
        //resultantForceX += Configuration.K_TANG * horizBorderOverlap * p.getVelocity().getX();
        
        double vertBorderOverlap = 0;
        if(p.getPosition().getX() - p.getRadius() < 0) {
        	vertBorderOverlap = (p.getRadius() - Math.abs(p.getPosition().getX()));
        	resultantForceX += Configuration.K_NORM * vertBorderOverlap;
        } else if(p.getPosition().getX() + p.getRadius() > Configuration.BOX_WIDTH) {
        	vertBorderOverlap = p.getRadius() - Math.abs(p.getPosition().getX() - Configuration.BOX_WIDTH);
        	//System.out.println("VERT OVERLAP " + vertBorderOverlap + " " + p.getPosition());
        	resultantForceX += - Configuration.K_NORM * vertBorderOverlap;
        }
				
        // resultantForceY += Configuration.K_TANG * vertBorderOverlap * p.getVelocity().getY();
				
				// if (vertBorderOverlap > 0) System.out.println(vertBorderOverlap);

        
				resultantForceY += p.getMass() * Configuration.GRAVITY;
				p.calculatePressure(resultantForceN);
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
