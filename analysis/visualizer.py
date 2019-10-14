import numpy as np
import matplotlib.pyplot as plt
from analyzer import calculateCollisionFrequency, calculateCollisionTimesAverage, calculateProbabilityCollisionTimesDistribution, calculateProbabilityVelocities, calculateDiffusion, calculateKineticEnergy, calculateExitsValues
from parser import parseDirectoryFromArgs, parseModeFromArgs, parseTimesFile, parseDirectory
from calculator import errorFn, discreteRange, PDF, mb, averageLists, stdevLists, beverloo
import os
import pickle

OUTPUT_FOLDER = 'output'

def saveFig(fig, name):
  if not os.path.exists(OUTPUT_FOLDER):
    os.makedirs(OUTPUT_FOLDER)
  fig.savefig(f'{OUTPUT_FOLDER}/{name}.png')
  
def ex3_1(simulations):
  for simulation in simulations:
    print(f'Simulacion: {simulation.name}')
    print(f'Frecuencia de colisiones (#/s):  {calculateCollisionFrequency(simulation)}')
    print(f'Promedio de tiempos de colision:  {calculateCollisionTimesAverage(simulation)}')
    times,edges = calculateProbabilityCollisionTimesDistribution(simulation)

    fig, ax = plt.subplots()
    ax.hist(times, bins=20, weights=np.ones_like(times) / len(times)) 
    ax.set_xlabel('Tiempos de colisión (s)')
    ax.set_ylabel('Distribución de probabilidad')
    ax.set_title(f'Movimiento Browniano (N={len(simulation.steps[0].particles)})') 
    fig.tight_layout()

    saveFig(fig, f'{simulation.name}--3_1')

def ex3_2(simulations):
  for simulation in simulations:
    print(f'Simulacion: {simulation.name}')
    speeds, listOfSpeedsTime0 = calculateProbabilityVelocities(simulation)

    # grafica el ultimo tercio
    fig, ax = plt.subplots()
    ax.hist(speeds, weights=np.ones_like(speeds) / len(speeds), bins=20) 
    ax.set_xlabel('Modulo de las velocidades (m/s)')
    ax.set_ylabel('Distribución de probabilidad')
    ax.set_title(f'Movimiento Browniano (N={len(simulation.steps[0].particles)}) - Ultimo tercio de tiempo') 
    fig.tight_layout()

    saveFig(fig, f'{simulation.name}--3_2')
    
    # grafica en t=0
    fig, ax = plt.subplots()
    ax.hist(listOfSpeedsTime0, weights=np.ones_like(listOfSpeedsTime0) / len(listOfSpeedsTime0), bins=20) 
    ax.set_xlabel('Modulo de las velocidades (m/s)')
    ax.set_ylabel('Distribución de probabilidad')
    ax.set_title(f'Movimiento Browniano (N={len(simulation.steps[0].particles)}) - t=0') 
    fig.tight_layout()

    saveFig(fig, f'{simulation.name}--3_2--initial')

def ex3_4(simulations):
  diffusionSlope, diffusionB, averageSquaredDistances, deviations = calculateDiffusion(simulations)
  print(f'Coeficiente de difusion aproximado: {diffusionSlope}')

  fig, ax = plt.subplots()
  x_axis = [ x + len(averageSquaredDistances) for x in range(len(averageSquaredDistances)) ]
  markers, caps, bars = ax.errorbar(x_axis, averageSquaredDistances, yerr=deviations, capsize=5, capthick=2, fmt="o", zorder=1, markersize=2) 
  ax.set_xlabel('Step')
  ax.set_ylabel('DCM = <z^2>')
  ax.set_title(f'Movimiento Browniano (N={len(simulations[0].steps[0].particles)}) - Ultima mitad del tiempo') 
  fig.tight_layout()
  
  # loop through bars and caps and set the alpha value
  [bar.set_alpha(0.5) for bar in bars]
  # [cap.set_alpha(0.5) for cap in caps]


  # Create linear regresion
  x = np.linspace(min(x_axis),max(x_axis),1000)
  y = diffusionSlope*(x - len(averageSquaredDistances))+diffusionB
  ax.plot(x,y, '--', zorder=2,linewidth=2)
  ax.legend(loc='upper left')

  saveFig(fig, '3_4')

def error(simulations):
  diffusionSlope, diffusionB, averageSquaredDistances, deviations = calculateDiffusion(simulations)
  print(f'Coeficiente de difusion aproximado: {diffusionSlope}')
  
  fig, ax = plt.subplots()
  y_axis, x_axis = errorFn(range(len(averageSquaredDistances)),averageSquaredDistances)
  ax.plot([x * 10 ** 5 for x in x_axis], y_axis) 
  ax.set_xlabel('C (10^-5)')
  ax.set_ylabel('Error')
  ax.set_title(f'Error del ajuste por función lineal') 
  fig.tight_layout()
  saveFig(fig, 'error')


def ex2_2(simulations):
  print(f'simulations: {len(simulations)}')
  for simulation in simulations:
    print(f'steps #: {len(simulation.steps)}')
    print("Starting to calculate percentages\n")

    percentages = [step.firstChamberPercentage() for step in simulation.steps]
    print(percentages[:10])


    print("Starting to calculate indexes\n")
    xs = discreteRange(0,len(simulation.steps)*0.1, 0.1)
    print("Plotting\n")
    fig, ax = plt.subplots()
    ax.plot(xs, percentages, '.',markersize=2)
    ax.plot(xs, [0.5] * len(xs), '--')
    ax.set_xlabel('Tiempo (s)')
    ax.set_ylabel('Fracción en el recinto izquierdo')
    fig.tight_layout()

    plt.show()
    saveFig(fig, '2_2')

def ex2_4(simulations):
  print(f'simulations: {len(simulations)}')
  for simulation in simulations:
    velocities = [step.speeds() for step in simulation.getSecondHalf()]
    velocities = [item for sublist in velocities for item in sublist] #flatten
    bin_size = 200
    print("Plotting\n")
    fig, ax = plt.subplots()
    values = np.histogram(velocities, density=True, bins=bin_size)
    bin_centres = (values[1][:-1] + values[1][1:])/2.
    bin_centres_x = (values[0][:-1] + values[0][1:])/2.
    ax.plot(bin_centres, values[0], 'o', markersize=2)

    adjustment = [mb(x) for x in bin_centres]
    ax.plot(bin_centres, adjustment, '-')

    ax.set_xlabel('Velocidad (m/s)')
    ax.set_ylabel('PDF')
    fig.tight_layout()

    saveFig(fig, '2_4')

    # plot error
    results, rang = errorFn(bin_centres, values[0])
    fig, ax = plt.subplots()
    ax.plot(rang, results, 'o', markersize=2) 
    ax.set_ylabel('Error')
    ax.set_xlabel('Parámetro Libre (a)')
    fig.tight_layout()

    saveFig(fig, '2_4_error')

def tp5_e1a():
  # Calculo del caudal
  timesList = parseDirectory('analysis/exits', parseTimesFile)
  windowSize = 30 # particles

  for time in timesList:
    times = time.steps
    Q = []
    for i in range(len(times) - windowSize):
      q = windowSize / (times[i+windowSize] - times[i])
      Q.append(q)
    time.setQ(Q)

  fig, ax = plt.subplots(figsize=(16,4))
  ax.set_ylabel('Caudal [particulas/s]')
  ax.set_xlabel('Tiempo [s]')
  for time in timesList:
    print(f'Name: {time.name}; Average: {time.getAverage()}; Error: {time.getError()}')
    ax.plot(time.steps[:-windowSize], time.Q, label=f'D = {time.name}')

  ax.legend()
  fig.tight_layout()
  saveFig(fig, '1_1a--caudales')

  # Beverloo curva

  xs = [time.hole for time in timesList]
  ys = [time for time in timesList]
  errs = [time.getError() for time in timesList]
  results, rang = errorFn(xs, ys)

  fig, ax = plt.subplots()
  ax.set_ylabel('ECM')
  ax.set_xlabel('Parámetro C')
  ax.plot(rang, results, 'o-', markersize=2) 
  # ax.errorbar(float(time.name), time.getAverage(), yerr=time.getError())
  fig.tight_layout()
  saveFig(fig, '1_1a--error')

  # Beverloo values
  fig, ax = plt.subplots()
  xs = [time.hole for time in timesList]
  ys = [time.getAverage() for time in timesList]
  errs = [time.getError() for time in timesList]
  bvs = [beverloo(x, 1.84) for x in xs]
  ax.set_ylabel('Caudal promedio [particulas/s]')
  ax.set_xlabel('Ancho de apertura [m]')
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, fmt='o-', markersize=2, zorder=1,capsize=5, capthick=2)
  ax.plot(xs, bvs, 'rx', markersize=6, zorder=10) 
  fig.tight_layout()
  saveFig(fig, '1_1a--adjusted')
  # plt.show()

def tp5_e1b(simulations):
    dt = 0.005 # seconds
    leftOffset = 0
   
    fig, ax = plt.subplots(figsize=(16,4))
    ax.set_yscale('log')
    ax.set_ylabel('Energía cinética [J]')
    ax.set_xlabel('Tiempo [s]')
    fig.tight_layout()

    for simulation in simulations:
      kineticEnergy = calculateKineticEnergy(simulation)
      xs = [x * dt for x in range(len(kineticEnergy))]
      ax.plot(xs[leftOffset:], kineticEnergy[leftOffset:], '-', label=f'D = {simulation.name}', linewidth=1)
    ax.legend()

    saveFig(fig, f'1_1b')

def tp5_e1c(simulations):
    dt = 0.005 # seconds
    leftOffset = 0
   
    fig, ax = plt.subplots(figsize=(16,4))
    ax.set_yscale('log')
    ax.set_ylabel('Energía cinética [J]')
    ax.set_xlabel('Tiempo [s]')
    fig.tight_layout()

    for simulation in simulations:
      kineticEnergy = calculateKineticEnergy(simulation)
      xs = [x * dt for x in range(len(kineticEnergy))]
      ax.plot(xs[leftOffset:], kineticEnergy[leftOffset:], '-', label=f'Kt = {simulation.name} x 10^5', linewidth=1)
    ax.legend()

    saveFig(fig, f'1_1c')


def run():
  print("python analysis/visualizer.py . 6")
  print("python analysis/visualizer.py analysis/results 7")
  print("python analysis/visualizer.py analysis/results 8")
  print("Las imágenes se guardan en la carpeta output de la raiz del proyecto.")
  print("Parse mode")
  mode = parseModeFromArgs()

  print("Parse simulations")
  if os.path.exists(f'{mode}.tmp'):
    print("File exists!")
    file = open(f'{mode}.tmp', 'rb')
    simulations = pickle.load(file)
    file.close()
  elif mode != 6:
    file = open(f'{mode}.tmp', 'wb')
    simulations = parseDirectoryFromArgs()
    print("Saving file")
    pickle.dump(simulations, file)
    file.close()

  if mode == 1:
    ex3_4(simulations)
  elif mode == 2:
    ex3_1(simulations)
    ex3_2(simulations)
  elif mode == 3:
    error(simulations)
  elif mode == 4:
    ex2_2(simulations)
  elif mode == 5:
    ex2_4(simulations)
  elif mode == 6:
    tp5_e1a()
  elif mode == 7:
    tp5_e1b(simulations)
  elif mode == 8:
    tp5_e1c(simulations)

run()