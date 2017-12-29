# Ghoulean's SVModManager

Mod Manager for Shadowverse

## Usage

1. Ensure that there is a `mods/` directory in the same directory as the tool. If not, create it.
2. Ensure that there is a `backup/` directory in the same directory as the tool. If not, create it.
3. Download and place mods in the "mods" directory. Ensure that each mod has a valid `mod.json` file.
4. Run the tool.

## Mods

File format for mods

### mod.json

In an attempt to maintain compatibility with [iluvredwall's mod installer](https://github.com/iluvredwall/SVMod) the `mod.json` uses mostly the same format. However, several key differences exist from iluvredwall's `mod.json` files.

The `mod.json` file contains metadata for the mod in json format. Currently, the following keys are used:

| Key         | Type             | Description |
| ----------- | ---------------- | ----------- |
| name        | string           | Name of mod |
| description | string           | Description of mod |
| authors     | array of strings | List of authors |
| version     | string           | Version number string |
| copy\_files | object           | An object specifying files to copy |
| preview     | object           | An object specifying preview files of mod files. This key can be excluded if the mod does not contain previews |

The keys of `copy_files` and `preview` are the directory names of the Shadowverse game. The values of the keys are an array of strings corresponding to the mod file and the preview files. Preview files must share the same name as its corresponding mod file, but it does not need to share the same file extension.

Example mod directory tree:

```
mods
└-sample mod folder
  ├-a
  │ ├-file1.unity3d
  │ ├-file2.unity3d
  │ ├-file3.unity3d
  │ ├-file1.png
  │ └-file3.jpg
  ├-v
  │ ├-file5.acb
  │ ├-file6.acb
  │ ├-file5.mp3
  │ └-file6.wav
  └-mod.json 
```

Note: `file2.unity3d` does not have a preview.

## Disclaimer

Modifying the game files is against [Shadowverse Terms of Service Article 5.3](https://shadowverse.com/terms.php). The creator of this tool does not condone or absolve any illegal conduct relating to Cygames or any other company. This tool is in no way associated with or endorsed by Cygames. Tool is provided as-is and is not responsible for anything that may happen to the user or their accounts as a result of using this tool. 