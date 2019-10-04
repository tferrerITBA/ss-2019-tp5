from ovito.io import import_file
from ovito.vis import VectorDisplay
from ovito.modifiers import *

node = import_file("C:\\Users\\Marcos\\git\\ss-2019-tp5\\ovito_output.xyz", multiple_frames = True, columns = 
	['Particle Identifier', 'Radius', 'Position.X', 'Position.Y', 'Velocity.X', 'Velocity.Y',
	 'Color.R', 'Color.G', 'Color.B'])
# id:I:1:radius:R:1:pos:R:2:velo:R:2:color:R:3
node.add_to_scene()
node.compute()
