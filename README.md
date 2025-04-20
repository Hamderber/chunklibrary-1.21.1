
Chunk Library
=======

A library focused on tracking a chunk's age since being loaded and implementing chunk regeneration depending on the externally set ConfigAPI settings. By itself, this mainly just tracks a chunk's age and allows for manual chunk resetting. For full use, you want to use https://www.curseforge.com/minecraft/mc-mods/unclaimed-chunks-regenerate or write something that utilizes this library's features!

KEY FEATURES:
Adds NeoForge events for initial chunk loading.
Adds hook for regenerating a chunk in a given dimension
Adds /chunklibrary commands for regenerating a chunk, changing it/the world's age, etc.
Supports modded dimensions
Injects a random seed into the ores, trees, and animals upon chunk regeneration
Teleports and protects entities from suffocation damage for 100 ticks following a chunk regenerating
Public methods and a ConfigAPI to allow for external mod hooks (KubeJS, non-AccessTransformer Mixin, etc.)

NOTE: For performance and compatibility, chunk regeneration occurs when it is being loaded. This means that to  regenerate a chunk, it must first be unloaded. This SIGNIFICANTLY reduces performance overhead. Entities are preserved, but blocks AND structure features are regenerated as if the chunk was never loaded before. This behaviour also ensures that compatability is entirely maintained.
 
This mod directly interfaces with Minecraft's internal chunk loading/generating features. When random seeds are injected, the same amount of random.next()/etc. are used which maintains the original Minecraft seed determinism. Additionally, because of where the hooks are used and a lack of overwriting features, this mod should be compatable with everything. For performance, air blocks are scanned when a chunk is first generated and cached. On a rolling, ConfigAPI-configurable basis, chunks that are at 90% of their age maturity are randomly selected and scanned for changes. Only after a ConfigAPI-configurable threshold of +/- total air block change is met will a chunk actually be flagged for regeneration. This saves overhead by not needlessly regenerating old, still-untouched chunks.