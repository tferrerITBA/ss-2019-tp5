package ar.edu.itba.ss.tpe5;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Particle implements Cloneable {
	
	private static int count = 0;
	
	private int id;
	private double radius;
	private int color;
	private Point2D.Double position;
	private Point2D.Double velocity;
	private Set<Particle> neighbors;
	
	public Particle(double radius, int color) {
		this.id = count++;
		this.radius = radius;
		this.color = color;
		this.position = new Point2D.Double();
		this.velocity = new Point2D.Double();
		this.neighbors = new HashSet<>();
	}
	
	private Particle(int id, double radius, int color, double x, double y, double vx, double vy) {
		this.id = id;
		this.radius = radius;
		this.color = color;
		this.position = new Point2D.Double(x, y);
		this.velocity = new Point2D.Double(vx, vy);
		this.neighbors = new HashSet<>();
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(!(o instanceof Particle))
			return false;
		Particle other = (Particle) o;
		return this.id == other.getId();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		return "id: " + id + "; radius: " + radius + " ; color: " + color + " ; x: " + position.x
				+ " ; y: " + position.y + " ; vx: " + velocity.x + " ; vy: " + velocity.y;
	}
	
	@Override
	public Particle clone() {
		return new Particle(id, radius, color, position.getX(), position.getY(), velocity.getX(), velocity.getY());
	}

	public int getId() {
		return id;
	}

	public double getRadius() {
		return radius;
	}

	public int getColor() {
		return color;
	}

	public Point2D.Double getPosition() {
		return position;
	}

	public void setPosition(double x, double y) {
		position.x = x;
		position.y = y;
	}

	public Point2D.Double getVelocity() {
		return velocity;
	}

	public void setVelocity(double vx, double vy) {
		velocity.x = vx;
		velocity.y = vy;
	}
	
	public double getVelocityAngle() {
		return Math.atan2(velocity.y, velocity.x);
	}
	
	public Set<Particle> getNeighbors() {
		return Collections.unmodifiableSet(neighbors);
	}
	
	public void clearNeighbors() {
		neighbors.clear();
	}
	
	public void addNeighbor(Particle p) {
		if(p == null)
			return;
		neighbors.add(p);
	}

}
