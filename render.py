from ovito.io import import_file
from ovito.vis import VectorDisplay
from ovito.modifiers import *

node = import_file("ovito_output.xyz", multiple_frames = True, columns = 
	['Particle Identifier', 'Radius', 'Position.X', 'Position.Y', 'Velocity.X', 'Velocity.Y',
	 'Vector Color.R', 'Vector Color.G', 'Vector Color.B'])
# id:I:1:radius:R:1:pos:R:2:velo:R:2:color:R:3
node.source.particle_properties.position.display.enabled = False
node.add_to_scene()

modifier = CalculateDisplacementsModifier()
modifier.reference.load("ovito_output.xyz", multiple_frames = True, columns = 
	['Particle Identifier', 'Radius', 'Position.X', 'Position.Y', 'Velocity.X', 'Velocity.Y',
	 'Vector Color.R', 'Vector Color.G', 'Vector Color.B'])
modifier.affine_mapping = CalculateDisplacementsModifier.AffineMapping.ToCurrent
modifier.frame_offset = 1
modifier.use_frame_offset = True
modifier.vector_display.enabled = True
modifier.vector_display.reverse = True
modifier.vector_display.shading = VectorDisplay.Shading.Flat
modifier.vector_display.scaling = 25.0
modifier.vector_display.width = 0.1
modifier.vector_display.alignment = VectorDisplay.Alignment.Base

node.modifiers.append(modifier)
node.compute()
