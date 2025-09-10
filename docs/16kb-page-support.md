# 16 KB page-size support (Android 15+)

This document explains the root cause, the fix, and how to verify support for 16 KB memory page-size devices. It’s intended for future maintainers/agents working on this repository.

## Summary of the issue

Emulator/Studio showed this warning when running or analyzing an APK built from this project:

> APK app-x86_64-debug.apk is not compatible with 16 KB devices. Some libraries have LOAD segments not aligned at 16 KB boundaries: lib/x86_64/libimage_processing_util_jni.so

Android 15+ devices may use 16 KB pages. All ELF LOAD segments in shipped native libraries (.so) must have p_align >= 16 KB and the APK must be zip-aligned at a 16 KB boundary for uncompressed libs.

## Root cause in this project

- The project doesn’t build any native code locally (no CMakeLists.txt/ndk-build). All native libs are transitively included from dependencies.
- The misaligned library was `libimage_processing_util_jni.so`, which comes from `androidx.camera:camera-core` 1.3.x.
- A resolution strategy in Gradle had pinned CameraX to 1.3.4, preventing upgrades.
- Packaging/zipalign flags cannot change the ELF program header alignment inside a prebuilt .so.

## What fixed it

1. Upgrade CameraX to 1.4.0 (ships 16 KB–aligned .so files):
   - `androidx.camera:camera-core:1.4.0`
   - `androidx.camera:camera-camera2:1.4.0`
   - `androidx.camera:camera-lifecycle:1.4.0`
   - `androidx.camera:camera-view:1.4.0`
2. Remove the `resolutionStrategy.force(...)` block that pinned older CameraX artifacts.
3. Remove unused `externalNativeBuild` flags from `defaultConfig` (they were no-op since we don’t build native code here).
4. Keep uncompressed native libs and ABI splits as configured (these are fine):
   - `packaging.jniLibs.useLegacyPackaging = false`
   - `splits.abi { include("arm64-v8a", "x86_64"); isUniversalApk = true }`

Result: All .so files in all APK variants (arm64-v8a, x86_64, universal) are aligned (2**14) and pass zipalign checks.

## How to verify (Windows)

Build:

- Powershell
  - `./gradlew.bat assembleDebug`

Check zip alignment at 16 KB:

- `C:\Users\<you>\AppData\Local\Android\Sdk\build-tools\36.0.0\zipalign.exe -v -c -P 16 4 app\build\outputs\apk\debug\app-x86_64-debug.apk`
- Repeat for `app-arm64-v8a-debug.apk` and `app-universal-debug.apk`.
- Expect: `lib/.../*.so (OK)` and `Verification successful` at the end.

Check ELF LOAD p_align values (no NDK required) with a small PowerShell helper:

1) Extract the APK (PowerShell treats APK as zip):

```powershell
$apk = "app\build\outputs\apk\debug\app-x86_64-debug.apk"
$zip = "$env:TEMP\app-x86_64-debug.zip"
Copy-Item $apk $zip -Force
$out = "$env:TEMP\apk_x86_64_out"
Remove-Item -Recurse -Force $out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $out | Out-Null
Expand-Archive -Path $zip -DestinationPath $out
$so = Join-Path $out "lib\x86_64\libimage_processing_util_jni.so"
```

2) Read the LOAD segments and ensure Align >= 16384 (2**14):

```powershell
$code = @'
param([string]$Path)
$fs = [System.IO.File]::OpenRead($Path)
$br = New-Object System.IO.BinaryReader($fs)
try {
  $br.ReadBytes(16) | Out-Null           # e_ident
  $br.ReadBytes(8)  | Out-Null           # e_type..e_version
  $br.ReadUInt64()  | Out-Null           # e_entry
  $e_phoff  = $br.ReadUInt64()           # e_phoff
  $br.ReadUInt64() | Out-Null            # e_shoff
  $br.ReadUInt32() | Out-Null            # e_flags
  $br.ReadUInt16() | Out-Null            # e_ehsize
  $e_phentsize = $br.ReadUInt16()
  $e_phnum     = $br.ReadUInt16()
  $br.ReadBytes(6) | Out-Null            # rest
  $LOAD = 1
  $rows = @()
  for ($i=0; $i -lt $e_phnum; $i++) {
    $fs.Seek($e_phoff + $i*$e_phentsize, 'Begin') | Out-Null
    $p_type   = $br.ReadUInt32()
    $p_flags  = $br.ReadUInt32()
    $p_offset = $br.ReadUInt64()
    $p_vaddr  = $br.ReadUInt64()
    $p_paddr  = $br.ReadUInt64()
    $p_filesz = $br.ReadUInt64()
    $p_memsz  = $br.ReadUInt64()
    $p_align  = $br.ReadUInt64()
    if ($p_type -eq $LOAD) {
      $rows += [pscustomobject]@{Index=$i; Offset=$p_offset; VAddr=$p_vaddr; Align=[int64]$p_align}
    }
  }
  $rows
}
finally { $br.Close(); $fs.Close() }
'@
$script = "$env:TEMP\read_elf_loads.ps1"
Set-Content -Path $script -Value $code -Encoding ASCII
& powershell -NoProfile -ExecutionPolicy Bypass -File $script -Path $so
```

- Expect `Align` to be 16384 for every LOAD row. Repeat for `lib\arm64-v8a\libimage_processing_util_jni.so` and other .so files if desired.

## How to verify (NDK tools)

If the Android NDK is installed, you can use `llvm-objdump` (replace NDK_VERSION and OS):

- Windows: `...\ndk\NDK_VERSION\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-objdump.exe -p <path-to-so> | Select-String -Pattern "LOAD"`
- macOS: `.../ndk/NDK_VERSION/toolchains/llvm/prebuilt/darwin-x86_64/bin/llvm-objdump -p <path-to-so> | grep LOAD`
- Linux: `.../ndk/NDK_VERSION/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-objdump -p <path-to-so> | grep LOAD`

Ensure every `align` field is `2**14`.

## If you actually build native code here in the future

- Prefer NDK r28+ (16 KB by default).
- For NDK r27: enable flexible page sizes (e.g., CMake arg `-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON`).
- For r26 or lower: link with `-Wl,-z,max-page-size=16384` (and `-Wl,-z,common-page-size=16384` for very old NDKs).

Example CMake snippet:

```cmake
# Align to 16 KB when using older NDKs/toolchains
# target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")
```

## Common pitfalls

- Analyzing an old APK: make sure you open the latest build from `app/build/outputs/apk/debug`.
- Assuming zipalign fixes ELF alignment: it doesn’t—zipalign handles APK entries, not ELF program headers inside .so files.
- Forcing dependency versions: avoid pinning older artifacts that ship non-compliant native libs.

## Current configuration (as of the fix)

- AGP: 8.12.x
- Build-Tools: 36.0.0
- Kotlin: 2.0.x
- CameraX: 1.4.0
- `packaging.jniLibs.useLegacyPackaging = false`
- ABI splits include `arm64-v8a` and `x86_64`, plus a universal APK during debug builds.

## Outcome

After the changes above, all native libraries in split and universal APKs are aligned on 16 KB boundaries (ELF LOAD `align` = 2**14) and zipalign verification succeeds. Studio’s APK Analyzer no longer reports the 16 KB alignment warning for native libraries.

