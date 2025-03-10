# UCGT Geometry Editor
## Introduction
UCGT Geometry Editor is a model compiling and decompiling tool. It supports the following:

- Car models, including racers, traffic, cops, wheels, generic parts: UC PC/X360 from/to Z3D and OBJ
- World models, including props, buildings, streamed chops: UC PC from/to Z3D (full support) and OBJ (partial support due to OBJ file format limits)
- NIS models, including perps, cars: UC PC from/to Z3D and OBJ (partial support)

## Command-line version usage
### Basics
The program runs in a loop, in which you can directly type commands. A reminder of the possible commands is displayed when opening the tool and can be brought back with the `help` or `?` command. The tool tries to parse what you type as a command, and displays an error if it isn't recognized or written properly.
The loop will continue indefinitely until you type `exit`, which closes the editor (and the command prompt window, if you ran it from a batch file).

The supported commands are the following :
- `compile <OBJ/Z3D/folder> <BIN output>` - compile a single 3D model or a folder and its/their corresponding config(s) to a BIN file containing one geometry per 3D model.
- `decompile <BIN source> [OBJ/Z3D output]` - extract a BIN file containing a single geometry to a 3D model and a configuration file.
- `compress <BIN file>` - compress an existing geometry file by parts using RefPack Maximum.
- `decompress <BIN file>` - decompress an existing geometry file. Useful for advanced hex editing and debugging.
- `dump <source> <destination folder> [file type] [filter]` - extract all geometries contained into the source BIN/BUN file to the output folder. File type is whether to export OBJ or Z3D, if left blank both will be exported, and filter will allow to export only matching blocks and names (X0, Road, Chop, XBu, etc).
- `replace <source folder> <destination> [blocks definitions]` - compile and replace all geometries in the destination with 3D models from the source folder. If needed, it can also update blocks definitions offsets (eg edit the map without unpacking), in that case, [blocks definitions] will be the path to the L8R_MW2.BUN file
- `convert <TXT CTK config> [INI UCGT config]` - convert a CTK .txt config to an UCGT .ini config
- `script <file>` - load a script containing multiple of these commands

To compile from an OBJ, you need a configuration file, just like NFS-CarToolkit. These are covered later in this Readme.

Decompiling a Geometry will generate a Wavefront OBJ, an associated MTL material library and an INI configuration file to compile it back.

If you have existing projects which use CTK, you can convert their configuration file. Be sure to check them afterwards to indicate all missing information.

Decompiling and compiling models from other files, such as map stream, is possible as of 1.2.0. Create a folder to dump editable 3d models to using `dump`, and put them back in the existing file using `replace` ! Note : for map models, you'll need to include the STREAML8R_MW2.BUN file as destination and the L8R_MW2.BUN file as blocks definition, or the game will crash looking for data that will be in a different place. Other note : you can't add NEW models since the replacement process checks for the file name, however you can add new meshes to existing models but these will probably not get loaded at all. Some compiling settings planned for cars will not be usable or may cause unwanted behavior.

### Arguments
If you run the program with arguments, it'll try to parse them as a valid command, execute it and close afterwards, without ever waiting for user input. Combined with a batch file, this allows you to setup one-click compiling, for instance, or take advantage of the batch syntax to e.g. recompile every Geometry in a folder.

_Note that if you run the compiler from a batch file, you probably won't be able to see any error message as the compiler closes itself once the command executed._

### Scripting
You can create simple scripts to execute multiple commands one after the other directly in UCGT, without needing external tools. Write them with any text editor, they can be saved in any format and must contain the commands in plain text, separated by line breaks, as you'd type them within the tool. Commenting these scripts is possible in any way you want, as the compiler only reacts to lines starting with one of the commands' keywords.

## Configuration files
The configuration file works by line : each line starts with a keyword determining what the tool will find behind.

### Settings
This part uses the keyword `SETTING` ; several settings can be written next to each other on a single line. It contains multiple settings concerning compiling, which are the following :

_Base_
- `CarName` - **mandatory** : sets the car name in the output Geometry.
- `FileName` - sets the file name in the output Geometry (no effect).
- `BlockName` - sets the block name in the output Geometry (no effect).
- `UseOffsetsTable` - whether to use a part offsets table (like in car models) or not (like in world models). Setting this to false will disable compression.
- `Platform` - sets the platform to use for mesh encoding. Default: PC. Possible values: PC, X360.
- `MakeDataBlock` - adds metadata to the file including all the settings after this one and shader usages. Useful if you want to pack up models with specific settings as a BIN file and replace them in the world map later on. Possible values : true or false. Note: unless you add the setting twice, this will not spread.
- `AutoReplaceWorldLODs` - whether to replace other world objects with a different LOD. Possible values: true or false. With this set to true, replacing an object in Yxxx will also replace its LODs in Xxxx, Wxxx and Uxxx.

_Compression_
- `UseMultithreading` - optional, defaults to true: use single-threaded or multi-threaded compression for car parts, multi-threaded significantly decreases the time needed to compile, especially when using RefPack and high compression settings. Possible values: true or false.
- `CompressionType` - optional, defaults to RefPack: set the compression type used for parts. Possible values : RefPack/RFPK, RawDecompressed/RAWW.
- `CompressionLevel` - optional, defaults to Low: set the compression level used. Has no effect if the compression type isn't RefPack. High or better recommended when your model is finalized. Possible values : Minimum, Low, Medium, High, VeryHigh, Ultra, Maximum.

_Optimization_
- `RemoveInvalid` - optional, defaults to true : removes invalid parts that don't fit with the game's naming scheme. Possible values : true or false. Due to UCGT's flexible approach, this is very basic and will only remove parts without a KIT and valid LOD.
- `RemoveUselessAutosculpt` - optional, defaults to true : removes useless _T0 parts when there's no actual morph zones to reduce file size. Possible values : true or false.
- `OptimizeMaterials` - optional, defaults to true : removes some unnecessary data in material declarations to help reduce file size. Possible values : true or false. Setting this to false may allow CTK to read the model, sometimes.
- `SortAllByName` - optional, defaults to true : sorts parts, markers, materials etc. by name. If set to false, materials will be sorted using the configuration file's order. Possible values : true or false.
- `CopyMissingLODs` - optional, defaults to false : if one or multiple LODs are missing, copies the mesh from a higher LOD. Possible values : true or false.
- `MakeLodD` - optional, defaults to false : if the KIT00 lod D is missing, copies it from lod C. It is highly advised to make a lod D manually ; setting this option to true will allow multiple lod C parts to be copied as lod D, making it unnecessarily heavy and potentially causing issues. Possible values : true or false.

_Mesh_
- `VertexColors` - optional, defaults to Import : import vertex colors (baked AO) from OBJ, generate them with an integrated method, or don't include them. Possible values : Import, Generate/Calculate, No.
- `Tangents` - optional, defaults to High : whether to calculate vertex tangents for normalmapped materials. High recommended, for Manual see the Materials section. Possible values : Off/No/None, Low/Optimized/ByShaderUsage, High/ByTextureUsage, Manual/PerMaterial, All/On/ForceCalculate.
- `FlipV` - optional, defaults to false : whether to flip the vertical axis on UV maps. Use in case your textures end up upside down. Possible values : true or false.

_Experimental fixes_
- `ForceAsFix` - optional: force-fixes autosculpt on the specified part if it doesn't get compiled correctly (this happens when there's the same amount of vertices on every morph zone but the welding is different). You can put this setting multiple times. Possible values: any part name.
- `FixAutosculptNormals` - optional, defaults to true: attempts to fix normals on zero area triangles, greatly improving the look of some Autosculpt parts, including vanilla. Possible values: true or false.
- `ProtectModel` - optional, defaults to false: removes plain text part names, effectively making it hard/tedious to steal your model. Possible values: true or false.
- `CheckModel` - optional, defaults to true: performs various quality checks on the model and reports possible issues. Possible values: true or false.

Example settings :

`SETTING	CarName=AUD_RS4_STK_08`

`SETTING  CompressionType=RefPack CompressionLevel=Low`

### Materials
This part uses the keyword `MATERIAL` and contains materials definitions, one per line. A material definition contains the following, separated by spaces or tabs :
- The material's name in the model.
- The shader and shader usage, in the format `SHADER=Usage` with no space inbetween.
- One or multiple textures with the corresponding texture usages, in the format `TEXTURE=USAGE`.
- If tangents are set to manual, include `UseTangents=true` in this part.

The material name, shader name and textures names are nonrestricted.

The possible shader usages are the following :

_Regular_
- Diffuse : use for most basic materials.
- DiffuseAlpha/Alpha : use for basic materials with transparency.
- car_a_nzw : unknown for now, will get a better name when I find more about it.
- DiffuseNormal/Normal : use for basic materials with normal.
- DiffuseNormalAlpha/NormalAlpha : use for basic materials with both alpha and normal.

_Skinnable_
- DiffuseSwatch/Swatch : use for carskin without damage mapping.
- DiffuseNormalSwatch/NormalSwatch : use for carskin and carbonfiber, probably anything skinnable/damageable.
- DiffuseNormalSwatchAlpha/NormalSwatchAlpha : unused in vanilla models, probably the same as above with transparency.

_Emissive_
- DiffuseSelfIllumination/DiffuseGlow/SelfIllumination/Glow : use for head- and taillights.
- DiffuseSelfIlluminationAlpha/DiffuseGlowAlpha/SelfIlluminationAlpha/GlowAlpha : use for brakelight glass.

_Traffic_
- TrafficDiffuse/Traffic : use for basic materials on traffic cars.
- TrafficDiffuseAlpha/TrafficAlpha : use for transparent traffic car materials.
- TrafficDiffuseNormal/TrafficNormal : use for traffic car materials with normals, including carskin.

In most cases, you can invert two names, the compiler should still understand it (e.g. AlphaNormal works the same as NormalAlpha).

Texture usages must fit with the shader usage, otherwise they might be ignored. The possible texture usages are the following :
- DIFFUSE : simply use the texture as diffuse (if the shader usage is DiffuseAlpha, the alpha channel is taken from this texture too).
- NORMAL : use as normalmap.
- SWATCH : use as paintable/skinnable vinyl placeholder.
- OPACITY/ALPHA : use as opacity map.
- SELFILLUMINATION : use the alpha channel as glow map, the texture is applied but takes another alpha channel if needed (which can be the DIFFUSE texture).
- VOLUMEMAP : untested, but may work on several shader usages.
- MISCMAP1 : used for CAR_SCRATCH texture for the damage on carskin, may be usable for some other shader usages, acts as a second diffuse mapping.


Example materials :

`MATERIAL	DECAL_BADGING_BADGING_BADGING_N	DECAL=DiffuseNormalAlpha	%_BADGING=DIFFUSE	%_BADGING=OPACITY	%_BADGING_N=NORMAL	FERenderingOrder=2`

`MATERIAL	BRAKELIGHT_BRAKELIGHT_OFF_BRAKELIGHT_O	BRAKELIGHT=DiffuseGlow	%_KIT00_BRAKELIGHT_OFF=DIFFUSE	%_KIT00_BRAKELIGHT_ON=SELFILLUMINATION`

`MATERIAL	DULLPLASTIC_MISC	DULLPLASTIC=Diffuse	%_MISC=DIFFUSE`

### Position markers
This part uses the keyword `MARKER` and contains position marker definitions, one per line. Position markers both must have a positioning mesh in the model, usually a cube, which name starts with "_". They also need the following definition, separated by spaces or tabs :
- The marker's positioning cube's name in the model.
- The marker's ingame name.
- The part the marker must be attached to, partial matches will work.
- Three Euler angles to rotate the marker, separated by spaces or tabs (pay great attention to these as they differ from the ones you may be used to !)
- If needed, the marker's scale, 1 is 100% (in all axes or per axis).

Example markers :

`MARKER	_BRAKELIGHT_LEFT	BRAKELIGHT_LEFT	KIT00_BASE	0	-90	-180` will assign the marker to KIT00_BASE_A, KIT00_BASE_B, KIT00_BASE_C, etc.

`MARKER	_LICENSE_PLATE_REAR_KIT00	LICENSE_PLATE_REAR	KIT00_BUMPER_REAR_A	0	-90	-180	0.979	1	0.979`

`MARKER	_COPLIGHTWHITE_KIT00	COPLIGHTWHITE	KIT00_BASE_A	0	0	0	101.599`

### Autosculpt links
This part uses the keyword `ASLINK`. An Autosculpt link makes it so that other parts and zones morph at the same time as you morph a given part. These are typically used in KITW01, and CTK never allowed to keep them intact, hence the issue where only the front bumper morphs in a CTK recompiled model. Autosculpt link config data contains the following :
- The name of the base part which will make other parts morph.
- The name of each part you want to morph along the base part, a comma, the zone to morph from repeated 2 times, the zone to morph to repeated 2 times, each part being separated with spaces or tabs.

Example Autosculpt links :

`ASLINK	KITW01_BUMPER_FRONT_A	KITW01_FENDER_FRONT_LEFT_A,1,1,1,1	KITW01_FENDER_FRONT_RIGHT_A,1,1,1,1	KITW01_BUMPER_REAR_A,1,1,1,1	KITW01_SKIRT_LEFT_A,1,1,1,1	KITW01_SKIRT_RIGHT_A,1,1,1,1`

`ASLINK	KIT11_BUMPER_FRONT_A	KIT11_FENDER_FRONT_LEFT_A,4,4,3,3	KIT11_FENDER_FRONT_RIGHT_A,4,4,3,3`

`ASLINK	KIT09_BUMPER_REAR_A	KIT09_BODY_A,1,1,1,1	KIT09_BODY_A,3,3,3,3	KIT09_BODY_A,4,4,4,4`

### Part copy, renaming or removal
This part uses the keywords `COPY`, `RENAME` or `DELETE`. 

Rename can be used to manually rename a single part, like you can do in a CTK config, or bulk rename. First put the name you have in the model, then the name you want it to be changed to, partial matches will work. 

Copy can be used in the same way and will copy the given part to one or multiple ones

Delete can be used in the same way to delete one or multiple parts based on a full or partial match with the part name.

Copy samples :

`COPY	WHEEL_TIRE_FRONT	WHEEL_TIRE_REAR` will copy all front wheels to rear ones, including Autosculpt zones

`COPY	KIT00_MUFFLER	KIT11_MUFFLER	KITW04_MUFFLER` will copy all KIT00 mufflers to KIT11 and KITW04


Rename samples : 

`RENAME	KITW01_WIDEBODY_A	KITW01_BODY_A`

`RENAME	STYLE04_HOOD	KIT06_HOOD` would rename all LODs and Autosculpt zones at the same time.

Delete samples :

`DELETE	DELETEME`

`DELETE	KIT99` prevents any part containing KIT99 from being compiled.

`DELETE	_D` would be a bad idea : it would remove all LOD D parts but also doors, driver which also contain _D : better be more specific.

### Rendering priority
The way render priority works in UC is still unknown, however UCGT includes some tools that should help fix such issues with trial and error.

First, please note that the order in which you declare materials in the configuration file matters. A transparent material that's declared AFTER another one will render correctly on top. In general, something with a higher priority should render properly in front of something with a lower one.

Then, if the material order doesn't fix your issue, each texture in a material can get a render priority. Use the following syntax : `<texture name>=<texture usage>,<priority>` with `priority` being an integer.

Finally, you can use a material-specific settings, `FERenderingOrder=<integer>` that can help you get around rendering issues in frontend, including ACROSS parts.

Samples :

`MATERIAL	HEADLIGHTGLASS_KIT00	HEADLIGHTGLASS=DiffuseAlpha		%_KIT00_HEADLIGHT_GLASS_OFF=DIFFUSE	%_KIT00_HEADLIGHT_GLASS_ON=SELFILLUMINATION	FERenderingOrder=0`

`MATERIAL	InteriorDecal	INTERIOR=DiffuseAlphaNormal	%_INTERIOR=DIFFUSE	%_INTERIOR=OPACITY,5	%_INTERIOR_N=NORMAL,4`

`MATERIAL	WINDSHIELD_WINDOW_FRONT	WINDSHIELD=DiffuseAlpha	WINDOW_FRONT=DIFFUSE	%_SKIN1=SWATCH	FERenderingOrder=4`

`//at the bottom to be see through`

`MATERIAL	Badge	DECAL=DiffuseNormalAlpha	%_BADGING=DIFFUSE	%_BADGING_N=NORMAL`


## Common settings combinations
### Cars and general car meshes
To compile a racer car, cop car or general car meshes such as SPOILER, the following options have to be used:

`CarName=<your car's XNAME here>` (unless you already entered every full part and texture name in both the model and the configuration file)

`UseOffsetsTable=true`

`CompressionType=RefPack`, `CompressionLevel=Maximum` or `CompressionType=RawDecompressed` (other compression types such as JDLZ and HUFF may work but are currently unsupported by UCGT)

`SortAllByName=true`

Materials to be used must have one of the following shader usages: `car`, `car_a`, etc.

For the sake of convenience, UCGT also accepts alternative names detailed in Materials below.

To compile wheels, use the same as above without the CarName.

### Traffic cars
To compile traffic cars, these settings have to be used:

`CarName=<your car's XNAME here>` (unless you already entered every full part and texture name in both the model and the configuration file)

`UseOffsetsTable=true`

`CompressionType=RawDecompressed`

`SortAllByName=true`

Materials to be used must have one of the following shader usages: `car_t`, `car_t_a` and `car_t_nm`.

For the sake of convenience, UCGT also accepts alternative names detailed in Materials below.

### Stream and NIS models
To compile other meshes such as NIS models (partial support) and stream models, use these settings:

`UseOffsetsTable=false`

`RemoveInvalid=false`

`FileName=<file to replace>` (eg `eLabScenery_XOs_Sawhorse_1.bin`)

`BlockName=<block name>` (eg `Y0`)

Materials in world and NIS models are slightly different from cars, most of them are governed by a shader usage and not so much by material shaders like cars.