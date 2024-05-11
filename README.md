# UCGT - Undercover Geometry Tool
This repo aims at filling gaps in NFS Undercover modding enlightened during the development of the Undercover Exposed mod.
As long as you explicitly credit me, you can freely reuse portions of this code or take inspiration from it.

It currently contains the following projects :
- GeomDump, a helper for creating other tools (working)
- CtkFixer, a shader and texture swapper eg for materials transparency (working, soon deprecated)
- DBMPPlus/fire, an advanced DBModelParts editor (working)
- a collisions and damage files editor (in progress)
- a geometry file editor meant to replace CTKFixer, fix its issues and improve its capabilities (in progress)

This repo uses code from the following GitHub repos, converted to Java by myself.
- NFSTools (?) : BIN and VLT hashers
- Speedreflect/Nikki : serialized DBModelParts and Collisions interoperability
- Gibbed.RefPack : RefPack archive compression and decompression algorithms (with help of RefPack-Tool)
- OpenNFSTools : EA JDLZ archive compression and decompression algorithms (with help of quickbms's source for decompression)
