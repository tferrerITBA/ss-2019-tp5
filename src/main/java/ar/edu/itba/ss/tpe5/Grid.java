package ar.edu.itba.ss.tpe5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Grid {
	
	private List<Particle> particles;
	private int areaBorderLength;
	private double interactionRadius;
	private List<List<GridSection>> grid;
	
	public Grid(int areaBorderLength, double interactionRadius, List<Particle> particles) {
		this.areaBorderLength = areaBorderLength;
		this.interactionRadius = interactionRadius;
		this.particles = particles;
		this.grid = new ArrayList<>();
		
		int m = calculateMaximumGridSectionBorderCount();
		if(!Configuration.isOptimalM()) {
			if(Configuration.getM() > m) {
				throw new RuntimeException("M violates Cell Index Method Algorithm.");
			}
			m = Configuration.getM();
		}
		
		for(int i = 0; i < m; i++) {
			grid.add(new ArrayList<>());
			for(int j = 0; j < m; j++) {
				grid.get(i).add(new GridSection(i, j));
			}
		}
		
		updateGridSections();
	}
	
	public void executeOffLatice() {
		for(int i = 0; i < Configuration.getTimeLimit(); i++) {
			List<Particle> updatedParticles = new ArrayList<>(Configuration.getParticleCount());
			calculateAllParticlesNeighbors();
			if(Configuration.isSingleRunMode())
				Configuration.writeOvitoOutputFile(i, particles);
			updateParticles(updatedParticles);
			setParticles(updatedParticles);
			updateGridSections();
		}
	}
	
	private void updateParticles(List<Particle> updatedParticles) {
		for(Particle p : particles) {
			Particle updatedParticle = p.clone();
			double newPositionX = p.getPosition().getX() + p.getVelocity().getX() * 1;
			if(newPositionX < 0 || newPositionX > areaBorderLength)
				newPositionX = (newPositionX + areaBorderLength) % areaBorderLength;
			double newPositionY = p.getPosition().getY() + p.getVelocity().getY() * 1;
			if(newPositionY < 0 || newPositionY > areaBorderLength)
				newPositionY = (newPositionY + areaBorderLength) % areaBorderLength;
			updatedParticle.setPosition(newPositionX, newPositionY);
			
			double accumVelocityX = p.getVelocity().getX();
			double accumVelocityY = p.getVelocity().getY();
			for(Particle n : p.getNeighbors()) {
				accumVelocityX += n.getVelocity().getX();
				accumVelocityY += n.getVelocity().getY();
			}
			double eta = Configuration.getEta();
			Random r = new Random();
			double newAngle = Math.atan2(accumVelocityY / (p.getNeighbors().size() + 1), accumVelocityX / (p.getNeighbors().size() + 1))
					+ (-eta/2 + r.nextDouble() * eta);
			double newVelocityX = Math.cos(newAngle) * Configuration.getVelocity();
			double newVelocityY = Math.sin(newAngle) * Configuration.getVelocity();
			updatedParticle.setVelocity(newVelocityX, newVelocityY);
			
			updatedParticles.add(updatedParticle);
		}
	}
	
	private void calculateAllParticlesNeighbors() {
		for(List<GridSection> gridRow : grid) {
			for(GridSection gridSection : gridRow) {
				for(int i = 0; i < gridSection.getParticles().size(); i++) {
					calculateParticleNeighbors(gridSection.getParticles().get(i), gridSection);
				}
			}
		}
	}
	
	private void updateGridSections() {
		for(List<GridSection> gridRow : grid) {
			for(GridSection gridSection : gridRow) {
				gridSection.getParticles().clear();
			}
		}
		double gridSectionBorderLength = ((double) areaBorderLength) / Configuration.getM();
		for(Particle p : particles) {
			int particleGridSectionRow = (int) ((areaBorderLength - p.getPosition().y) / gridSectionBorderLength);
			int particleGridSectionColumn = (int) (p.getPosition().x / gridSectionBorderLength);
			grid.get(particleGridSectionRow).get(particleGridSectionColumn).addParticle(p);
		}
	}
	
	private void calculateParticleNeighbors(Particle p, GridSection gridSection) {
		int middleColumnIndex = gridSection.getColumn();
		int middleRowIndex = gridSection.getRow();
		
		/* Particles within the same grid section */
		calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(middleRowIndex).get(middleColumnIndex));
		if(getM() == 1)
			return;
		
		/* Particles with neighboring grid section's particles */
		if(getM() <= 2) {
			int leftColumnIndex = gridSection.getColumn() - 1;
			int rightColumnIndex = gridSection.getColumn() + 1;
			int topRowIndex = gridSection.getRow() - 1;
			int bottomRowIndex = gridSection.getRow() + 1;
			
			if(topRowIndex >= 0)
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(topRowIndex).get(middleColumnIndex));
			if(leftColumnIndex >= 0)
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(middleRowIndex).get(leftColumnIndex));
			if(rightColumnIndex < grid.get(0).size())
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(middleRowIndex).get(rightColumnIndex));
			if(bottomRowIndex < grid.size())
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(bottomRowIndex).get(middleColumnIndex));
			if(topRowIndex >= 0 && leftColumnIndex >= 0)
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(topRowIndex).get(leftColumnIndex));
			if(topRowIndex >= 0 && rightColumnIndex < grid.get(0).size())
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(topRowIndex).get(rightColumnIndex));
			if(bottomRowIndex < grid.size() && leftColumnIndex >= 0)
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(bottomRowIndex).get(leftColumnIndex));
			if(bottomRowIndex < grid.size() && rightColumnIndex < grid.get(0).size())
				calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(bottomRowIndex).get(rightColumnIndex));
		} else {
			int leftColumnIndex = ((gridSection.getColumn() - 1) + getM()) % getM();
			int rightColumnIndex = ((gridSection.getColumn() + 1) + getM()) % getM();
			int topRowIndex = ((gridSection.getRow() - 1) + getM()) % getM();
			int bottomRowIndex = ((gridSection.getRow() + 1) + getM()) % getM();
			
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(topRowIndex).get(middleColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(middleRowIndex).get(leftColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(middleRowIndex).get(rightColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(bottomRowIndex).get(middleColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(topRowIndex).get(leftColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(topRowIndex).get(rightColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(bottomRowIndex).get(leftColumnIndex));
			calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(bottomRowIndex).get(rightColumnIndex));
		}
	}
	
	private void calculateParticleNeighborsWithGridSection(Particle p1, int particleGridSectionRow, int particleGridSectionColumn, 
			GridSection gridSection) {
		for(Particle p2 : gridSection.getParticles()) {
			if(!p1.equals(p2)) {
				
				double sameGridHorizontalDistance = Math.abs(p1.getPosition().x - p2.getPosition().x);
				double horizontalDistance = Math.min(
						sameGridHorizontalDistance, areaBorderLength - sameGridHorizontalDistance
					);
				
				double sameGridVerticalDistance = Math.abs(p1.getPosition().y - p2.getPosition().y);
				double verticalDistance = Math.min(
						sameGridVerticalDistance, areaBorderLength - sameGridVerticalDistance
					);
				
				double borderToBorderDistance = Math.sqrt(
						Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2))
							- p1.getRadius() - p2.getRadius();
				
				if(borderToBorderDistance <= interactionRadius) {
					p1.addNeighbor(p2);
				}
			}
		}
	}

	private int calculateMaximumGridSectionBorderCount() {
		int maxGridSectionBorderCount = (int) (areaBorderLength / interactionRadius);
		return (maxGridSectionBorderCount == 0)? 1 : maxGridSectionBorderCount;
	}
	
	public void printParticlesByGridSection() {
		for(List<GridSection> gridRow : grid) {
			for(GridSection gridSection : gridRow) {
				System.out.println("Particles in (" + gridSection.getRow() + ", " + gridSection.getColumn() + "):");
				for(Particle p : gridSection.getParticles())
					System.out.println(p.getId() + ": (" + p.getPosition().x + ", " + p.getPosition().y + ")");
			}
		}
	}
	
	public void printParticlesWithNeighbors() {
		for(Particle p : particles) {
			System.out.println("Neighbors for particle " + p.getId() + "at (" + p.getPosition().x + ", " + p.getPosition().y + "):");
			for(Particle n : p.getNeighbors()) {
				System.out.println(n.getId());
			}
		}
	}
	
	public double getDensity() {
		return particles.size() / Math.pow(areaBorderLength, 2);
	}
	
	public double getOrderParameter() {
		double accumVelocityX = 0;
		double accumVelocityY = 0;
		for(Particle p : particles) {
			accumVelocityX += p.getVelocity().getX();
			accumVelocityY += p.getVelocity().getY();
		}
		return Math.sqrt(Math.pow(accumVelocityX, 2) + Math.pow(accumVelocityY, 2)) 
				/ (Configuration.getParticleCount() * Configuration.getVelocity());
	}
	
	public List<Particle> getParticles() {
		return Collections.unmodifiableList(particles);
	}
	
	public void setParticles(List<Particle> newParticles) {
		Objects.requireNonNull(newParticles);
		particles = newParticles;
	}
	
	public int getM() {
		return grid.size();
	}
	
	public double getGridSectionLength() {
		return areaBorderLength / (double) getM();
	}
	
	private class GridSection {
		
		private int row;
		private int column;
		private List<Particle> particles;
		
		public GridSection(int row, int column) {
			this.row = row;
			this.column = column;
			particles = new ArrayList<>();
		}
		
		public void addParticle(Particle p) {
			particles.add(p);
		}
		
		public int getRow() {
			return row;
		}
		
		public int getColumn() {
			return column;
		}
		
		public List<Particle> getParticles() {
			return particles;
		}
		
	}
}
