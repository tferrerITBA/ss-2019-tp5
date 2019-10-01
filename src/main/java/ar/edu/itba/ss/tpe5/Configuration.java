package ar.edu.itba.ss.tpe5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Configuration {

	private static double timeStep;
	private static String staticFileName = "static_config.txt";
	private static String dynamicFileName = "dynamic_config.txt";
	private static Integer particleCount;
	private static Integer areaBorderLength;
	private static Double interactionRadius;
	private static boolean isOptimalM;
	private static Integer m = null;
	private static double velocity = 0.03;
	private static Double eta;
	private static Mode mode;
	private static int timeLimit;
	public static final int TEST_CYCLES = 20;
	public static final double ETA_TEST_STEP = 0.2;
	public static final double ETA_TEST_CYCLES = 25;
	public static final int DENSITY_TEST_PARTICLE_STEP = 200;
	public static final int DENSITY_TEST_MAX_PARTICLES = 4000;
	
	public static void requestMode() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		Integer selectedMode = null;
	    System.out.println("Enter Mode [0 -> Single Run; 1-> Common Test; 2 -> Eta Test; 3 -> Density Test]: ");
	    while(selectedMode == null || selectedMode < 0 || selectedMode > 3) {
	    	selectedMode = stringToInt(scanner.nextLine());
	    }
	    mode = Mode.valueOf(selectedMode).get();
	}
	
	public static void requestParameters() {
		Scanner scanner = new Scanner(System.in);
		
	    System.out.println("Enter Interaction Radius: ");
	    while(interactionRadius == null || interactionRadius <= 0) {
	    	interactionRadius = stringToDouble(scanner.nextLine());
	    }
	    
	    System.out.println("Enter M [0 -> Optimal]:");
    	Integer selectedM = null;
	    while(selectedM == null || selectedM < 0) {
	    	selectedM = stringToInt(scanner.nextLine());
	    }
	    m = selectedM;
	    isOptimalM = (m == 0);

		System.out.println("Enter Time Step:");
		Double selectedTimeStep = null;
		while(selectedTimeStep == null || selectedTimeStep <= 0) {
			selectedTimeStep = stringToDouble(scanner.nextLine());
		}
		timeStep = selectedTimeStep;

	    if(!isDensityTestMode()) {
			System.out.println("Enter Particle Count:");
			Integer selectedParticleCount = null;
			while (selectedParticleCount == null) {
				selectedParticleCount = stringToInt(scanner.nextLine());
			}
			particleCount = selectedParticleCount;
		}
	    if(!isEtaTestMode()) {
			 System.out.println("Enter Angle Noise (eta):");
	    	while(eta == null || eta <= 0) {
		    	eta = stringToDouble(scanner.nextLine());
		    }
		}
	    
	    System.out.println("Enter Area Length:");
    	Integer selectedAreaLength = null;
	    while(selectedAreaLength == null || selectedAreaLength <= 0) {
	    	selectedAreaLength = stringToInt(scanner.nextLine());
	    }
	    areaBorderLength = selectedAreaLength;
	    
	    System.out.println("Enter Time Limit:");
    	Integer selectedTimeLimit = null;
	    while(selectedTimeLimit == null || selectedTimeLimit <= 0) {
	    	selectedTimeLimit = stringToInt(scanner.nextLine());
	    }
	    timeLimit = selectedTimeLimit;
	    
	    scanner.close();
	}
	
	/* Parameters must have already been requested */
	public static void generateRandomInputFilesAndParseConfiguration(List<Particle> particles) {
		generateRandomInputFiles(particleCount, areaBorderLength);
		parseStaticConfiguration(particles);
		parseDynamicConfiguration(particles);
		if(isSingleRunMode())
			generateOvitoOutputFile();
	}
	
	private static void parseStaticConfiguration(List<Particle> particles) {
		try(BufferedReader br = new BufferedReader(new FileReader(staticFileName))) {
			String line = br.readLine();
			if((particleCount = stringToInt(line)) == null || particleCount < 1) {
				failWithMessage("Invalid or missing particle count (" + line + ").");
			}
			line = br.readLine();
			if((areaBorderLength = stringToInt(line)) == null || areaBorderLength < 1) {
				failWithMessage("Invalid or missing area border length (" + line + ").");
			}
			for(int i = 0; i < particleCount; i++) {
				line = br.readLine();
				if(line == null)
					failWithMessage("Particles do not match particle count.");
				String[] attributes = line.split(" ");
				attributes = removeSpaces(attributes);
				particles.add(validateParticle(attributes));
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//System.out.println("Static configuration loaded.");
	}
	
	private static void parseDynamicConfiguration(List<Particle> particles) {		
		try(BufferedReader br = new BufferedReader(new FileReader(dynamicFileName))) {
			String line = br.readLine();
			for(int i = 0; i < particleCount; i++) {
				line = br.readLine();
				if(line == null)
					failWithMessage("Particles do not match particle count.");
				String[] attributes = line.split(" ");
				attributes = removeSpaces(attributes);
				setDynamicParticleAttributes(particles.get(i), attributes);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//System.out.println("Dynamic configuration loaded.");
	}
	
	private static String[] removeSpaces(String[] array) {
		List<String> list = new ArrayList<>(Arrays.asList(array));
		List<String> filteredList = list.stream().filter(s -> !s.equals("") && !s.equals(" ")).collect(Collectors.toList());
		String[] newArray = new String[filteredList.size()];
		return filteredList.toArray(newArray);
	}
	
	private static Integer stringToInt(String s) {
		Integer i = null;
		try {
			i = Integer.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return i;
	}
	
	private static Double stringToDouble(String s) {
		Double d = null;
		try {
			d = Double.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return d;
	}
	
	private static void failWithMessage(String message) {
		System.err.println(message);
		System.exit(1);
	}

	private static Particle validateParticle(String[] attributes) {
		Double radius = null;
		if(attributes.length != 1
			|| (radius = stringToDouble(attributes[0])) == null || radius < 0) {
				failWithMessage(attributes[0] + " is an invalid Particle.");
			}
		return new Particle(radius);
	}
	
	private static void setDynamicParticleAttributes(Particle particle, String[] attributes) {
		Double x = null;
		Double y = null;
		Double vx = null;
		Double vy = null;
		if(attributes.length != 4
			|| (x = stringToDouble(attributes[0])) == null || x < particle.getRadius() || x > areaBorderLength - particle.getRadius()
			|| (y = stringToDouble(attributes[1])) == null || y < particle.getRadius() || y > areaBorderLength - particle.getRadius()
			|| (vx = stringToDouble(attributes[2])) == null
			|| (vy = stringToDouble(attributes[3])) == null) {
				failWithMessage(attributes[0] + ", " + attributes[1] + ", " + attributes[2] + ", " + attributes[3] + " are invalid attributes.");
			}
		particle.setPosition(x, y);
		particle.setVelocity(vx, vy);
	}
	
	private static void generateRandomInputFiles(int particleCount, int areaBorderLength) {
		generateRandomStaticInputFile(particleCount, areaBorderLength);
		generateRandomDynamicInputFile(particleCount, areaBorderLength);
	}
	
	private static void generateRandomStaticInputFile(int particleCount, int areaBorderLength) {
		File staticInputFile = new File(staticFileName);
		staticInputFile.delete();
		try(FileWriter fw = new FileWriter(staticInputFile)) {
			staticInputFile.createNewFile();
			fw.write(particleCount + "\n");
			fw.write(areaBorderLength + "\n");
			for(int i = 0; i < particleCount; i++) {
				fw.write("0\n");
			}
		} catch (IOException e) {
			System.err.println("Failed to create static input file.");
			e.printStackTrace();
		}
	}
	
	private static void generateRandomDynamicInputFile(int particleCount, int areaBorderLength) {
		File dynamicInputFile = new File(dynamicFileName);
		dynamicInputFile.delete();
		try(FileWriter fw = new FileWriter(dynamicInputFile)) {
			dynamicInputFile.createNewFile();
			fw.write("0\n");
			Random r = new Random();
			for(int i = 0; i < particleCount; i++) {
				double randomPositionX = areaBorderLength * r.nextDouble();
				double randomPositionY = areaBorderLength * r.nextDouble();
				double angle = 2 * Math.PI * r.nextDouble();
				double randomVelocityX = Math.cos(angle) * velocity;
				double randomVelocityY = Math.sin(angle) * velocity;
				fw.write(randomPositionX + " " + randomPositionY + " " + randomVelocityX + " " + randomVelocityY + "\n");
			}
		} catch (IOException e) {
			System.err.println("Failed to create dynamic input file.");
			e.printStackTrace();
		}
	}
	
	private static void generateOvitoOutputFile() {
		File outputFile = new File("./ovito_output.xyz");
		outputFile.delete();
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create Ovito output file.");
			e.printStackTrace();
		}
	}
	
	public static void writeOvitoOutputFile(int time, List<Particle> particles) {
		File outputFile = new File("ovito_output.xyz");
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			fw.write(particleCount + "\n");
			fw.write("Lattice=\"" + areaBorderLength + " 0.0 0.0 0.0 " + areaBorderLength 
				+ " 0.0 0.0 0.0 " + areaBorderLength 
				+ "\" Properties=id:I:1:radius:R:1:pos:R:2:velo:R:2:color:R:3 Time=" + time + ".0\n");
			for(Particle p : particles) {
				writeOvitoParticle(fw, p);
			}
		} catch (IOException e) {
			System.err.println("Failed to write Ovito output file.");
			e.printStackTrace();
		}
	}
	
	private static void writeOvitoParticle(FileWriter fw, Particle particle) throws IOException {
		fw.write(particle.getId() + " " + particle.getRadius() + " " + particle.getPosition().x + " "
				+ particle.getPosition().y + " " + particle.getVelocity().x + " " + particle.getVelocity().y + " ");
		
		double angle = particle.getVelocityAngle();
		fw.write(((Math.cos(angle) + 1) / 2) + " " + ((Math.sin(angle) + 1) / 2) + " " + Math.tan((angle + Math.PI) / 8));
		fw.write('\n');
	}
	
	public static void writeEtaTestResultsToOutputFile(List<Double> averageOrderParameters, List<Double> standardDeviations) {
		File outputFile = new File("multiple_tests_eta_output.txt");
		outputFile.delete();
		try(FileWriter fw = new FileWriter(outputFile)) {
			outputFile.createNewFile();
			for(int i = 0; i < averageOrderParameters.size(); i++) {
				fw.write(String.format(Locale.US, "%.2g", (ETA_TEST_STEP * i)) + " " + averageOrderParameters.get(i) 
				+ " " + standardDeviations.get(i) + "\n");
			}
		} catch (IOException e) {
			System.err.println("Failed to write multiple tests output file.");
			e.printStackTrace();
		}		
	}

	public static void writeDensityTestResultsToOutputFile(List<Double> averageOrderParameters, List<Double> standardDeviations) {
		File outputFile = new File("multiple_tests_density_output.txt");
		outputFile.delete();
		try(FileWriter fw = new FileWriter(outputFile)) {
			outputFile.createNewFile();
			int areaLength = Configuration.getAreaBorderLength();
			for(int i = 1; i < averageOrderParameters.size() + 1; i++) {
				fw.write((DENSITY_TEST_PARTICLE_STEP * i) / (double)(areaLength * areaLength)
						+ " " + averageOrderParameters.get(i-1)	+ " " + standardDeviations.get(i-1) + "\n");
			}
		} catch (IOException e) {
			System.err.println("Failed to write multiple tests output file.");
			e.printStackTrace();
		}
	}

	public static int getParticleCount() {
		return particleCount;
	}

	public static void setParticleCount(int particleCount) {
		Configuration.particleCount = particleCount;
	}

	public static int getAreaBorderLength() {
		return areaBorderLength;
	}
	
	public static double getInteractionRadius() {
		return interactionRadius;
	}
	
	public static Integer getM() {
		return m;
	}
	
	public static boolean isOptimalM() {
		return isOptimalM;
	}
	
	public static double getVelocity() {
		return velocity;
	}
	
	public static double getEta() {
		return eta;
	}
	
	public static void setEta(double newEta) {
		eta = newEta;
	}
	
	public static int getTimeLimit() {
		return timeLimit;
	}
	
	public static boolean isSingleRunMode() {
		return mode == Mode.SINGLE_RUN;
	}
	
	public static boolean isCommonTestMode() {
		return mode == Mode.COMMON_TEST;
	}
	
	public static boolean isEtaTestMode() {
		return mode == Mode.ETA_TEST;
	}
	
	public static boolean isDensityTestMode() {
		return mode == Mode.DENSITY_TEST;
	}

	public static double getTimeStep() {
		return timeStep;
	}
}
