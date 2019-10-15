import os

RESULTS_FOLDER = 'analysis/results'
DEFAULT_OUTPUT = 'ovito_output.xyz'
EXIT_OUTPUT = 'exit.txt'
EXIT_FOLDER = 'analysis/exits'
REPEAT = 1
TIME = 10 #seconds
SIMULATION = 'java -jar target/tpes-1.0-SNAPSHOT.jar < params.txt &'
# SIMULATION = 'java -jar target/tpes-1.0-SNAPSHOT.jar < params.txt'
REMOVE = f'rm -fr {RESULTS_FOLDER}'

# create results folder if it does not exist
# if os.path.exists(RESULTS_FOLDER):
# os.system(REMOVE)
# os.makedirs(RESULTS_FOLDER)

# Generate multiple simulations
VALUES = [0.15, 0.18,  0.22, 0.25]
# VALUES = [1, 2, 3, 4]
for simNum in VALUES:
  os.system(f'echo "{TIME}\n{simNum}\n{simNum}\n{2.0}\n" > params.txt')
  MOVE = f'mv {simNum}.xyz {RESULTS_FOLDER}/{simNum}.xyz'
  MOVE_EXIT = f'mv {simNum}.txt {EXIT_FOLDER}/{simNum}.txt'
  RM = f'rm {simNum}-input.txt'
  os.system(SIMULATION) # run simulation
  os.system("sleep 5")
  # os.system(MOVE) # store results
  # os.system(MOVE_EXIT) # store results
  # os.system(MOVE_EXIT) # store results
  # os.system(RM) # remove input file
