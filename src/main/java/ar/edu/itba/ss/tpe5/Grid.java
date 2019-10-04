package ar.edu.itba.ss.tpe5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Grid {
	
	private List<Particle> particles;
	private final double boxWidth;
	private final double boxHeight;
	private final int m;
	private final double interactionRadius;
	private final List<List<GridSection>> grid;
	
	public Grid(final List<Particle> particles) {
		this.interactionRadius = Configuration.getInteractionRadius();
		this.particles = particles;
		this.boxWidth = Configuration.BOX_WIDTH;
		this.boxHeight = Configuration.BOX_HEIGHT;
		this.grid = new ArrayList<>();
		
		this.m = calculateMaximumGridSectionBorderCount();
		
		// Inferior grid sections can have smaller height; width fits perfectly
		double gridSectionBorderLength = boxWidth / m;
		int gridSectionRows = (int) Math.round(Math.ceil(boxHeight / gridSectionBorderLength));
		for(int i = 0; i < gridSectionRows; i++) {
			grid.add(new ArrayList<>());
			for(int j = 0; j < m; j++) {
				grid.get(i).add(new GridSection(i, j));
			}
		}
		
		updateGridSections();
	}
	
	public void calculateAllParticlesNeighbors() {
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
		double gridSectionBorderLength = boxWidth / m;
		for(Particle p : particles) {
			int particleGridSectionRow = (int) ((boxHeight - p.getPosition().y) / gridSectionBorderLength);
			int particleGridSectionColumn = (int) (p.getPosition().x / gridSectionBorderLength);
			grid.get(particleGridSectionRow).get(particleGridSectionColumn).addParticle(p);
		}
	}
	
	private void calculateParticleNeighbors(Particle p, GridSection gridSection) {
		int middleColumnIndex = gridSection.getColumn();
		int middleRowIndex = gridSection.getRow();
		
		/* Particles within the same grid section */
		calculateParticleNeighborsWithGridSection(p, middleRowIndex, middleColumnIndex, grid.get(middleRowIndex).get(middleColumnIndex));
		if(m == 1)
			return;
		
		/* Particles with neighboring grid section's particles */
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
	}
	
	private void calculateParticleNeighborsWithGridSection(Particle p1, int particleGridSectionRow, int particleGridSectionColumn, 
			GridSection gridSection) {
		for(Particle p2 : gridSection.getParticles()) {
			if(!p1.equals(p2)) {
				double horizontalDistance = Math.abs(p1.getPosition().x - p2.getPosition().x);
				double verticalDistance = Math.abs(p1.getPosition().y - p2.getPosition().y);
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
		double[] r = largestRadiusPair(particles);
		int maxM = (int) (boxWidth / (interactionRadius + r[0] + r[1]));
		return (maxM == 0)? 1 : maxM;
	}
	
	private double[] largestRadiusPair(List<Particle> particles) {
		double[] r = new double[2];
		r[0] = particles.get(0).getRadius();
		if(particles.size() == 1)
			return r;
		r[1] = particles.get(1).getRadius();
		for(int i = 2; i < particles.size(); i++) {
			double radius = particles.get(i).getRadius();
			if(r[0] < radius) {
				r[1] = r[0];
				r[0] = radius;
			} else if(r[1] < radius) {
				r[1] = radius;
			}
		}
		return r;
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
	
	public List<Particle> getParticles() {
		return Collections.unmodifiableList(particles);
	}
	
	public void setParticles(List<Particle> newParticles) {
		Objects.requireNonNull(newParticles);
		particles = newParticles;
	}
	
	public double getGridSectionLength() {
		return boxWidth / m;
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
