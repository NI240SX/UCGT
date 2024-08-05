# UCGT Geometry Editor
## Command-line version usage
### Basics
The program runs in a loop, in which you can directly type commands. A reminder of the possible commands is displayed when opening the tool and can be brought back with the `help` or `?` command. The tool tries to parse what you type as a command, and displays an error if it isn't recognized or written properly.
The loop will continue indefinitely until you type `exit`, which closes the editor (and the command prompt window, if you ran it from a batch file).

The supported commands are the following :
- `compile <OBJ source> <BIN output>` - compile a Wavefront OBJ to a Geometry
- `decompile <BIN source> <OBJ output>` - extract a Geometry to a Wavefront OBJ
- `convert <TXT CTK config> [INI UCGT config]` - convert a CTK .txt config to an UCGT .ini config
- `script <file>` - load a script containing multiple of these commands

To compile from an OBJ, you need a configuration file, just like NFS-CarToolkit. These are covered later in this Readme.

Decompiling a Geometry will generate a Wavefront OBJ, an associated MTL material library and an INI configuration file to compile it back.

If you have existing projects which use CTK, you can convert their configuration file. Be sure to check them afterwards to indicate all missing information.

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

_Compression_
- `UseMultithreading` - optional, defaults to true : use single-threaded or multi-threaded compression for car parts, multi-threaded significantly decreases the time needed to compile, especially when using RefPack and high compression settings. Possible values : true or false.
- `CompressionType` - optional, defaults to RefPack : set the compression type used for parts. Possible values : RefPack/RFPK, RawDecompressed/RAWW.
- `CompressionLevel` - optional, defaults to Low : set the compression level used. Has no effect if the compression type isn't RefPack. High or better recommended when your model is finalized. Possible values : Minimum, Low, Medium, High, VeryHigh, Ultra, Maximum.

_Optimization_
- `RemoveInvalid` - optional, defaults to true : removes invalid parts that don't fit with the game's naming scheme. Possible values : true or false.
- `RemoveUselessAutosculpt` - optional, defaults to true : removes useless _T0 parts when there's no actual morph zones to reduce file size. Possible values : true or false.
- `OptimizeMaterials` - optional, defaults to true : removes some unnecessary data in material declarations to help reduce file size. Possible values : true or false.
- `SortAllByName` - optional, defaults to true : sorts parts, markers, materials etc. by name. If set to false, materials will be sorted using the configuration file's order. Possible values : true or false.
- `CopyMissingLODs` - optional, defaults to false : if one or multiple LODs are missing, copies the mesh from a higher LOD. Possible values : true or false.
- `MakeLodD` - optional, defaults to false : if the KIT00 lod D is missing, copies it from lod C. It is highly advised to make a lod D manually ; setting this option to true will allow multiple lod C parts to be copied as lod D, making it unnecessarily heavy and potentially causing issues. Possible values : true or false.

_Mesh_
- `VertexColors` - optional, defaults to Import : import vertex colors (baked AO) from OBJ, generate them with an integrated method, or don't include them. Possible values : Import, Generate/Calculate, No.
- `Tangents` - optional, defaults to High : whether to calculate vertex tangents for normalmapped materials. High recommended, for Manual see the Materials section. Possible values : Off/No/None, Low/Optimized/ByShaderUsage, High/ByTextureUsage, Manual/PerMaterial, All/On/ForceCalculate.
- `FlipV` - optional, defaults to false : whether to flip the vertical axis on UV maps. Use in case your textures end up upside down. Possible values : true or false.

_Experimental fixes_
- `ForceAsFix` - optional : force-fixes autosculpt on the specified part if it doesn't get compiled correctly (this happens when there's the same amount of vertices on every morph zone but the welding is different). You can put this setting multiple times. Possible values : any part name.
- `FixAutosculptNormals` - optional, defaults to true : attempts to fix normals on zero area triangles, greatly improving the look of some Autosculpt parts, including vanilla. Possible values : true or false.

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

Then, if the material order doesn't fix your issue, there's the keyword `PRIORITY` which sets a render priority to certain materials of certain parts. First put the partial or full part name, then for each material add a pair `<material>=<priority>`.

Finally, you can use two material-specific settings, `FERenderingOrder=<integer>` and `RenderingOrder=<integer>` that can help you get around rendering issues ACROSS parts, especially in frontend.

As an example, here's an extract of the configuration file I used for Undercover Exposed's Diablo SV. This car has decals inside the cabin behind the windows, as well as badges over multiple light glasses on bumpers, and a badge over a grill on the body.

`MATERIAL	HEADLIGHTGLASS_KIT00	HEADLIGHTGLASS=DiffuseAlpha		%_KIT00_HEADLIGHT_GLASS_OFF=DIFFUSE	%_KIT00_HEADLIGHT_GLASS_ON=SELFILLUMINATION	FERenderingOrder=0`

...

`MATERIAL	InteriorDecal	INTERIOR=DiffuseAlphaNormal	%_INTERIOR=DIFFUSE	%_INTERIOR_N=NORMAL`

`MATERIAL	EngineDecal	ENGINE=DiffuseAlphaNormal	%_ENGINE=DIFFUSE	%_ENGINE_N=NORMAL`

...

`MATERIAL	WINDSHIELD_WINDOW_FRONT	WINDSHIELD=DiffuseAlpha	WINDOW_FRONT=DIFFUSE	%_SKIN1=SWATCH	FERenderingOrder=4`

`MATERIAL	WINDSHIELD_WINDOW_LEFT_FRONT	WINDSHIELD=DiffuseAlpha	WINDOW_LEFT_FRONT=DIFFUSE	%_SKIN1=SWATCH`

`MATERIAL	WINDSHIELD_WINDOW_RIGHT_FRONT	WINDSHIELD=DiffuseAlpha	WINDOW_RIGHT_FRONT=DIFFUSE	%_SKIN1=SWATCH	FERenderingOrder=6`

`MATERIAL	WINDSHIELD_WINDOW_REAR	WINDSHIELD=DiffuseAlpha	WINDOW_REAR=DIFFUSE	%_SKIN1=SWATCH	FERenderingOrder=4`

`MATERIAL	WINDSHIELD_WINDOW_LEFT_REAR	WINDSHIELD=DiffuseAlpha	WINDOW_LEFT_REAR=DIFFUSE	%_SKIN1=SWATCH	FERenderingOrder=4`

`MATERIAL	WINDSHIELD_WINDOW_RIGHT_REAR	WINDSHIELD=DiffuseAlpha	WINDOW_RIGHT_REAR=DIFFUSE	%_SKIN1=SWATCH	FERenderingOrder=4`

...

`MATERIAL	GrilleA	GRILL=DiffuseAlpha	GRILL_02=DIFFUSE`

...

`//at the bottom to be see through`

`MATERIAL	Badge	DECAL=DiffuseNormalAlpha	%_BADGING=DIFFUSE	%_BADGING_N=NORMAL`

...

`PRIORITY	BRAKELIGHT	Badge=1`

`PRIORITY	BODY	Badge=2	GrilleA=3`

`PRIORITY	BUMPER	Badge=5	HEADLIGHTGLASS_KIT00=6`

`PRIORITY	BASE	EngineDecal=0	InteriorDecal=0`