
Chunk Library
=======

Consists of a few helper methods for getting the age of a chunk and levels/dimensions from strings. Implements a way to trigger regeneration of a chunk upon loading.

KEY FEATURES:

 - Adds NeoForge events for initial chunk loading.
 - Adds hook for regenerating a chunk in a given dimension
 - Adds /chunklibrary regenchunk <dimension:id> <x> <y> <z> command to regenerate a specific chunk

 NOTE:
 For performance and compatibility, chunk regeneration occurs when it is being loaded. This means that to regenerate a chunk, it must first be unloaded. This SIGNIFICANTLY reduces performance overhead. Entities are preserved, but blocks AND structure features are regenerated as if the chunk was never loaded before.