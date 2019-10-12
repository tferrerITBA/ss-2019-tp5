from ovito.io import import_file
from ovito.vis import VectorDisplay
from ovito.modifiers import *

node = import_file("C:\\Users\\Marcos\\git\\ss-2019-tp5\\ovito_output.xyz", multiple_frames = True, columns = 
    ['Particle Identifier', 'Radius', 'Position.X', 'Position.Y', 'Velocity.X', 'Velocity.Y'])
# id:I:1:radius:R:1:pos:R:2:velo:R:2:color:R:3
node.add_to_scene()

#cell = node.source.cell
#mat = cell.matrix.copy()
# cell vectors
#mat[0][0] = 0.4 # width
#mat[1][1] = 1.5 # height
# cell origin
#mat[0][3] = 0 # X origin
#mat[1][3] = 0 # Y origin

#cell.matrix = mat

modifier = AffineTransformationModifier(
    transform_particles = False,
    transform_surface = False,
    transform_vector_properties = False,
    transform_box = True,
    transformation = [[0.4, 0, 0, 0],
                      [0, 1.5, 0, 0],
                      [0, 0, 0, 0]]
)

node.modifiers.append(modifier)

node.compute()
