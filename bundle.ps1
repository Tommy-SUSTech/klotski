# === Configuration ===
$AppName     = "Klotski"
$MainClass   = "io.github.jimzhouzzy.klotski.lwjgl3.Lwjgl3Launcher"
$IconPath    = "icons\icon.ico"      # <-- Optional, must be .ico on Windows
$JarDir      = "lwjgl3\build\libs"
$OutputDir   = "bundled"

# === Run Gradle Build ===
Write-Host "Running Gradle build..."
$gradleCmd = if ($IsWindows) { ".\gradlew.bat" } else { "./gradlew" }
& $gradleCmd build

# === Find Latest JAR ===
$JarFile = Get-ChildItem "$JarDir\Klotski-*.jar" | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $JarFile) {
    Write-Error "No Klotski-*.jar found in $JarDir"
    exit 1
}

Write-Host "Using JAR: $($JarFile.FullName)"

# === Ensure output directory exists ===
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

# === jpackage ===
$jpackageArgs = @(
    "--name", $AppName
    "--input", $JarFile.DirectoryName
    "--main-jar", $JarFile.Name
    "--main-class", $MainClass
    "--type", "exe"
    "--dest", $OutputDir
    "--app-version", "1.0.0"
    "--vendor", "JimZhouZZY"
    "--java-options", "-XstartOnFirstThread"
)

# None null check for icon
if ($null -ne $IconPath -and $IconPath -ne "" -and (Test-Path $IconPath)) {
    $jpackageArgs += "--icon", $IconPath
}

Write-Host "Running jpackage..."
jpackage @jpackageArgs

Write-Host "`n✅ EXE bundling complete. Output in: $OutputDir"
