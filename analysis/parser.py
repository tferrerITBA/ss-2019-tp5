from models import Particle, Step, Simulation, Times
import glob
import sys
import os

def parseDirectoryFromArgs():
  return parseDirectory(sys.argv[1])

def parseModeFromArgs():
  return int(sys.argv[2])

def parseTimesFile(filename):
  times = [float(line.rstrip('\n')) for line in open(filename)]
  return Times(times, filename)

def parseFile(filename):
  lines = [line.rstrip('\n') for line in open(filename)]
  steps = []
  while len(lines) > 0:
    steps.append(parseStep(lines))
  return Simulation(steps, os.path.basename(filename))

def parseDirectory(directory, parse=parseFile):
  dirs = glob.glob(directory + '/*')
  dirs.sort()
  return [parse(f) for f in dirs]

def parseStep(lines):
  nextLines = int(lines.pop(0))
  time = float(lines.pop(0).split("Time=").pop())
  particles = [ parseParticle(lines.pop(0)) for _ in range(nextLines)]
  return Step(time, particles)

def parseParticle(line):
  properties = line.split(" ")
  particle = Particle(*properties)
  return particle