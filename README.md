# UCGT - Undercover Geometry Toolsuite
[This repo](https://github.com/NI240SX/UCGT) aims at filling gaps in NFS Undercover modding enlightened during the development of the Undercover Exposed mod.

As long as you explicitly credit me, you can freely reuse portions of this code or take inspiration from it.


If you wish to support my stuff, you can [buy me a coffee](https://ko-fi.com/ni240sx).


**JAVA 15 OR HIGHER IS REQUIRED TO RUN ANY OF THE FOLLOWING**

Download the latest version in the [Releases](https://github.com/NI240SX/Releases) page. 

All tools depend on shared dependencies, and as such are all bundled together.

##Included tools
### Geometry Editor

A model extractor/editor with a focus on NFS Undercover.

Works on game BINs and BUNs.

This editor is available in both CLI and GUI. 

[See associated Readme.](README%20-%20Geometry%20Editor.md)

### DBModelParts Editor

An advanced DBModelParts editor, built to be faster and more practical than Binary's.

Works on exported data from Binary.

### Collisions Editor

A viewer for Collisions data.

Works on exported data from Binary (serialized).


## UCGT uses modified code from the following
- NFSTools (?) : BIN and VLT hashers
- Speedreflect/Nikki : serialized DBModelParts and Collisions interoperability
- Gibbed.RefPack : RefPack archive compression and decompression algorithms (with help of RefPack-Tool)
- OpenNFSTools : EA JDLZ archive compression and decompression algorithms (with help of quickbms's source for decompression)
- JavaDDS : display of DDS textures in JavaFX 2D/3D
