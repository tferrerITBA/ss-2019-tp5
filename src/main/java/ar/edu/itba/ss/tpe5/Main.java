package ar.edu.itba.ss.tpe5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
	
	private static long startTime;
	
	public static void main(String[] args) {
		Configuration.requestMode();
		
		if(Configuration.isSingleRunMode()) {
			executeSingleRun();
		} else if(Configuration.isCommonTestMode()) {
			runMultipleTests();
		} else if(Configuration.isEtaTestMode()) {
			runMultipleTestsWithVariableEta();
		} else if(Configuration.isDensityTestMode()) {
			runMultipleTestsWithVariableDensity();
		}
		
		long endTime = System.nanoTime();
		System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
	}
	
	private static void executeSingleRun() {
		Configuration.requestParameters();
		startTime = System.nanoTime();
		List<Particle> particles = new ArrayList<>();
		Configuration.generateRandomInputFilesAndParseConfiguration(particles);
		Grid grid = new Grid(Configuration.getAreaBorderLength(), Configuration.getInteractionRadius(), particles);
		grid.executeOffLatice();
		
		System.out.println("Order parameter: " + grid.getOrderParameter());
	}
	
	private static void runMultipleTests() {
		Configuration.requestParameters();
		startTime = System.nanoTime();
		double orderParameterAccum = 0;
		for(int i = 0; i < Configuration.TEST_CYCLES; i++) {
			List<Particle> particles = new ArrayList<>();
			Configuration.generateRandomInputFilesAndParseConfiguration(particles);
			Grid grid = new Grid(Configuration.getAreaBorderLength(), Configuration.getInteractionRadius(), particles);
			grid.executeOffLatice();
			orderParameterAccum += grid.getOrderParameter();
		}
		System.out.println("Order Parameter after " + Configuration.TEST_CYCLES + " runs: " 
				+ (orderParameterAccum / Configuration.TEST_CYCLES));
	}
	
	private static void runMultipleTestsWithVariableEta() {
		Configuration.requestParameters();
		startTime = System.nanoTime();
		List<Double> averageOrderParameters = new ArrayList<>();
		List<Double> standardDeviations = new ArrayList<>();
		
		for(int i = 0; i <= Configuration.ETA_TEST_CYCLES; i++) {
			
			double eta = Configuration.ETA_TEST_STEP * i;
			Configuration.setEta(eta);
			double accumOrderParameter = 0;
			List<Double> calculatedOrderParameters = new ArrayList<>(Configuration.TEST_CYCLES);
			
			for(int j = 0; j < Configuration.TEST_CYCLES; j++) {
				List<Particle> particles = new ArrayList<>();
				Configuration.generateRandomInputFilesAndParseConfiguration(particles);
				Grid grid = new Grid(Configuration.getAreaBorderLength(), Configuration.getInteractionRadius(), particles);
				
				grid.executeOffLatice();
				accumOrderParameter += grid.getOrderParameter();
				calculatedOrderParameters.add(grid.getOrderParameter());
			}
			
			double averageOrderParameter = (accumOrderParameter / Configuration.TEST_CYCLES);
			averageOrderParameters.add(averageOrderParameter);
			
			double accumQuadraticDifference = 0;
			for(Double orderParameter : calculatedOrderParameters) {
				accumQuadraticDifference += Math.pow(orderParameter - averageOrderParameter, 2);
			}
			standardDeviations.add(Math.sqrt(accumQuadraticDifference / (Configuration.TEST_CYCLES - 1)));
			
			System.out.printf("Eta: %.2g", eta);
			System.out.println("; Order Parameter after " + Configuration.TEST_CYCLES + " runs: " + averageOrderParameter);
		}
		
		Configuration.writeEtaTestResultsToOutputFile(averageOrderParameters, standardDeviations);
	}

	private static void runMultipleTestsWithVariableDensity() {
		Configuration.requestParameters();
		startTime = System.nanoTime();
		List<Double> averageOrderParameters = new ArrayList<>();
		List<Double> standardDeviations = new ArrayList<>();

		for(int i = 1; i <= Configuration.DENSITY_TEST_MAX_PARTICLES / Configuration.DENSITY_TEST_PARTICLE_STEP; i++) {

			int particleCount = Configuration.DENSITY_TEST_PARTICLE_STEP * i;
			Configuration.setParticleCount(particleCount);
			double accumOrderParameter = 0;
			List<Double> calculatedOrderParameters = new ArrayList<>(Configuration.TEST_CYCLES);

			for(int j = 0; j < Configuration.TEST_CYCLES; j++) {
				List<Particle> particles = new ArrayList<>();
				Configuration.generateRandomInputFilesAndParseConfiguration(particles);
				Grid grid = new Grid(Configuration.getAreaBorderLength(), Configuration.getInteractionRadius(), particles);
				grid.executeOffLatice();
				accumOrderParameter += grid.getOrderParameter();
				calculatedOrderParameters.add(grid.getOrderParameter());
			}

			double averageOrderParameter = (accumOrderParameter / Configuration.TEST_CYCLES);
			averageOrderParameters.add(averageOrderParameter);

			double accumQuadraticDifference = 0;
			for(Double orderParameter : calculatedOrderParameters) {
				accumQuadraticDifference += Math.pow(orderParameter - averageOrderParameter, 2);
			}
			standardDeviations.add(Math.sqrt(accumQuadraticDifference / (Configuration.TEST_CYCLES - 1)));

			System.out.printf("N: " + particleCount + "; density: %.2g", Configuration.getParticleCount() 
					/ Math.pow(Configuration.getAreaBorderLength(), 2));
			System.out.println("; Order Parameter after " + Configuration.TEST_CYCLES + " runs: " + averageOrderParameter);
		}
		
		Configuration.writeDensityTestResultsToOutputFile(averageOrderParameters, standardDeviations);
	}

}
