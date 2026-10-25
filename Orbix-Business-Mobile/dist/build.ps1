<#
    Builds Orbix Manager Android APKs - the generic Orbix app and/or customer-branded apps.
    MAINTAINER TOOL - never shipped to a client. Run from Orbix-Business-Mobile.

      double-click dist\build.cmd                          # the easy way: asks everything below,
                                                           # and the window stays open at the end
      .\dist\build.ps1                                     # asks which brand(s), then each server
      .\dist\build.ps1 -Brand sighman                      # asks the server (Enter keeps the default)
      .\dist\build.ps1 -Brand sighman -Server http://13.51.26.114:8080
      .\dist\build.ps1 -Brand all                          # every brand under brands\
      .\dist\build.ps1 -Brand orbix -Server http://10.0.0.5:8080 -Tag qa   # generic app for QA
      .\dist\build.ps1 -Brand all -NonInteractive          # no questions: profile defaults only

    BRANDS. Each folder brands\<id>\ is one app: its name, colour, icon letter and DEFAULT
    server live in brand.json. Add a customer with tool\brand.py (it generates the icon,
    palette and Android resources), commit, then build here - no code or Gradle changes:

      python tool\brand.py new acme --name "Acme Manager" --company "Acme Ltd" `
          --mark A --color "#7C2D12" --server http://1.2.3.4:8080
      python tool\brand.py list

    THE SERVER is only a default - the phone can change it later (sign-in footer, 7 taps). It is
    resolved per brand as: -Server, else a question offering the profile's "server" (Enter keeps
    it). A customer brand must have one. The generic orbix brand may be left blank: the app then
    asks for the address on first launch. The address is checked (http/https, no API path) and
    probed - any HTTP answer, even 401, means the server is there; no answer asks before going on,
    so a typo does not ship.

    Steps: read the version from pubspec.yaml, run `flutter test` once, then per brand check its
    compiled profile (test\brand_test.dart with that brand's defines), run
    `flutter build apk --release --flavor <id>` and copy the APK to
    dist\apk\<AppName>-<version>[-<tag>].apk with a .json record beside it (brand, app id,
    version, commit, server, SHA-256, signing certificate). APKs are git-ignored.

    SIGNING. Every APK is signed with the Orbix Manager release key, never the debug key:
    android\key.properties (git-ignored) names the keystore, which lives outside the repo.
    Each APK's signature is verified after the build. Keep the keystore and its password
    backed up together - without them no update installs over an existing install.

    It refuses to overwrite an existing APK unless -Force.

    Requirements: Flutter with the Android toolchain (build-tools for apksigner), git,
    and android\key.properties.
#>

[CmdletBinding()]
param(
    # Brand id(s) to build, or 'all'. Omit to be asked.
    [string[]] $Brand,
    # Default server baked into the app. Only with a single brand. Omit to be asked.
    [string] $Server,
    # Extra file-name tag, e.g. 'qa' -> SighmanHaulage-1.0.0-qa.apk.
    [string] $Tag,
    # Never prompt: take -Server or the profile default, and fail when a customer has neither.
    [switch] $NonInteractive,
    # Do not probe the server (e.g. building on a machine that cannot reach it).
    [switch] $NoProbe,
    # Skip the test suite. For a quick local build only - never for a client.
    [switch] $SkipTests,
    # Overwrite an existing APK of this version.
    [switch] $Force
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$AppDir    = Split-Path -Parent $ScriptDir
$BrandsDir = Join-Path $AppDir 'brands'
$Pubspec   = Join-Path $AppDir 'pubspec.yaml'
$OutDir    = Join-Path $ScriptDir 'apk'
$ApiPath   = '/orbix-business-api'     # lib/core/config/server_config.dart
$BaseAppId = 'net.otapp.orbix.manager' # android/app/build.gradle.kts; customer = <base>.<id>
$Generic   = 'orbix'

function Write-Step { param([string]$m) Write-Host ''; Write-Host "==> $m" -ForegroundColor White }
function Write-Ok   { param([string]$m) Write-Host "  ok  $m" -ForegroundColor Green }
function Write-Warn { param([string]$m) Write-Host "  !!  $m" -ForegroundColor Yellow }
function Stop-WithError {
    param([string]$m)
    Write-Host ''; Write-Host 'ERROR' -ForegroundColor Red; Write-Host ''; Write-Host $m; Write-Host ''
    exit 1
}

# Run a native command in a directory; fail the build on a non-zero exit code.
function Invoke-In {
    param([string]$Dir, [string]$What, [scriptblock]$Command)
    Push-Location $Dir
    try {
        & $Command
        if ($LASTEXITCODE -ne 0) { Stop-WithError "$What failed (exit code $LASTEXITCODE). See the output above." }
    } finally { Pop-Location }
}

function Read-YesNo {
    param([string]$Question)
    $a = Read-Host "$Question [y/N]"
    return $a -match '^(y|yes)$'
}

# 'http://1.2.3.4:8080/orbix-business-api/' -> 'http://1.2.3.4:8080'. Returns $null when it is not an address.
function ConvertTo-ServerAddress {
    param([string]$Value)
    $v = "$Value".Trim()
    while ($v.EndsWith('/')) { $v = $v.Substring(0, $v.Length - 1) }
    if ($v.ToLowerInvariant().EndsWith($ApiPath)) { $v = $v.Substring(0, $v.Length - $ApiPath.Length) }
    while ($v.EndsWith('/')) { $v = $v.Substring(0, $v.Length - 1) }
    if ($v -notmatch '^(?i)https?://[A-Za-z0-9.\-]+(:\d{1,5})?$') { return $null }
    return $v
}

# Any HTTP status (401 included) = something is answering. Only no answer at all is a failure.
function Test-Server {
    param([string]$Address)
    try {
        $null = Invoke-WebRequest -Uri "$Address$ApiPath/login" -UseBasicParsing -TimeoutSec 8 -Method Get
        return 'answered'
    } catch [System.Net.WebException] {
        if ($null -ne $_.Exception.Response) { return "answered (HTTP $([int]$_.Exception.Response.StatusCode))" }
        return $null
    } catch {
        return $null
    }
}

function Get-Brand {
    param([string]$Id)
    $path = Join-Path $BrandsDir "$Id\brand.json"
    if (-not (Test-Path $path)) { return $null }
    $b = Get-Content $path -Raw -Encoding UTF8 | ConvertFrom-Json
    $defines = Join-Path $BrandsDir "$Id\generated\dart_defines.json"
    if (-not (Test-Path $defines)) {
        Stop-WithError "brands\$Id has no generated files. Run:  python tool\brand.py gen $Id"
    }
    $default = ''
    if ($null -ne $b.server) { $default = "$($b.server)" }
    [pscustomobject]@{
        Id       = $Id
        AppName  = "$($b.appName)"
        Company  = "$($b.company)"
        Default  = $default
        Defines  = $defines
        AppId    = $(if ($Id -eq $Generic) { $BaseAppId } else { "$BaseAppId.$Id" })
        FileBase = ("$($b.appName)" -replace '[^A-Za-z0-9]', '')
        Server   = ''
    }
}

# ---------------------------------------------------------------------------
Write-Step 'Checking tools'
foreach ($tool in 'flutter', 'git') {
    if ($null -eq (Get-Command $tool -ErrorAction SilentlyContinue)) { Stop-WithError "$tool is not installed or not on PATH." }
}
Write-Ok 'flutter, git found'

# The release key: android\key.properties -> a keystore outside the repo.
$KeyProps = Join-Path $AppDir 'android\key.properties'
if (-not (Test-Path $KeyProps)) {
    Stop-WithError ("No release signing key: $KeyProps is missing.`n`n" +
        "It names the Orbix Manager keystore (storeFile, storePassword, keyAlias, keyPassword).")
}
$StoreLine = Select-String -Path $KeyProps -Pattern '^storeFile=(.+)$' | Select-Object -First 1
if ($null -eq $StoreLine -or -not (Test-Path $StoreLine.Matches[0].Groups[1].Value.Trim())) {
    Stop-WithError "The keystore named in $KeyProps does not exist. Restore it from the backup."
}
Write-Ok 'release signing key found'

# apksigner, from the newest Android build-tools.
$SdkRoot = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA 'Android\Sdk')) |
    Where-Object { $_ -and (Test-Path (Join-Path $_ 'build-tools')) } | Select-Object -First 1
$ApkSigner = $null
if ($SdkRoot) {
    $ApkSigner = Get-ChildItem (Join-Path $SdkRoot 'build-tools') -Directory |
        Sort-Object { try { [version]$_.Name } catch { [version]'0.0' } } -Descending |
        ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
        Where-Object { Test-Path $_ } | Select-Object -First 1
}
if (-not $ApkSigner) { Stop-WithError 'apksigner not found - install Android SDK build-tools.' }
Write-Ok 'apksigner found'

$VersionLine = Select-String -Path $Pubspec -Pattern '^version:\s*(\S+)' | Select-Object -First 1
if ($null -eq $VersionLine) { Stop-WithError "No 'version:' line found in $Pubspec." }
$Version   = $VersionLine.Matches[0].Groups[1].Value
$BuildName = ($Version -split '\+')[0]
Write-Ok "version $Version"

$Known = @(Get-ChildItem $BrandsDir -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'brand.json') } |
    ForEach-Object { $_.Name } | Sort-Object)
if ($Known.Count -eq 0) { Stop-WithError "No brands under $BrandsDir." }

# ---------------------------------------------------------------------------
Write-Step 'Choosing brands'
if (-not $Brand -or $Brand.Count -eq 0) {
    if ($NonInteractive) { Stop-WithError "-Brand is required with -NonInteractive. Known: $($Known -join ', ')" }
    Write-Host ''
    for ($i = 0; $i -lt $Known.Count; $i++) {
        $b = Get-Brand $Known[$i]
        Write-Host ("  {0}) {1,-14} {2}" -f ($i + 1), $b.Id, $b.AppName)
    }
    Write-Host ''
    $answer = Read-Host 'Build which? (numbers or ids, comma-separated, or "all")'
    $Brand = @($answer -split '[,\s]+' | Where-Object { $_ } | ForEach-Object {
        if ($_ -match '^\d+$' -and [int]$_ -ge 1 -and [int]$_ -le $Known.Count) { $Known[[int]$_ - 1] } else { $_ }
    })
}
# "-Brand a,b" arrives as one element when run through powershell -File; split it either way.
$Brand = @($Brand | ForEach-Object { $_ -split '[,\s]+' } | Where-Object { $_ } | ForEach-Object { $_.ToLowerInvariant() })
if ($Brand -contains 'all') { $Brand = $Known }
$Brand = @($Brand | Select-Object -Unique)
$Unknown = @($Brand | Where-Object { $Known -notcontains $_ })
if ($Unknown.Count -gt 0) {
    Stop-WithError ("Unknown brand(s): $($Unknown -join ', '). Known: $($Known -join ', ').`n`n" +
        "Add one with:  python tool\brand.py new <id> --name ... --company ... --color ... --server ...")
}
if ($Server -and $Brand.Count -ne 1) { Stop-WithError '-Server applies to a single brand. Build several brands without it to be asked for each.' }
if ($Tag -and $Tag -notmatch '^[A-Za-z0-9\-]+$') { Stop-WithError '-Tag may contain only letters, digits and dashes.' }
$Brands = @($Brand | ForEach-Object { Get-Brand $_ })
Write-Ok "building: $(($Brands | ForEach-Object { $_.Id }) -join ', ')"

# ---------------------------------------------------------------------------
Write-Step 'Server addresses (a default only - the phone can change it later)'
foreach ($b in $Brands) {
    $isGeneric = $b.Id -eq $Generic
    Write-Host ''
    Write-Host "  $($b.AppName)  [$($b.Id)]" -ForegroundColor Cyan
    while ($true) {
        if ($Server) {
            $raw = $Server
        } elseif ($NonInteractive) {
            $raw = $b.Default
        } else {
            if ($b.Default) { $hint = "Enter = $($b.Default)" }
            elseif ($isGeneric) { $hint = 'Enter = none, the phone asks on first launch' }
            else { $hint = 'required' }
            $raw = Read-Host "  Server ($hint)"
            if (-not $raw.Trim()) { $raw = $b.Default }
        }

        if (-not "$raw".Trim()) {
            if ($isGeneric) { $b.Server = ''; Write-Ok 'no server baked in - the app asks on first launch'; break }
            if ($NonInteractive -or $Server) {
                Stop-WithError "$($b.Id) has no default server. Pass -Server, or set ""server"" in brands\$($b.Id)\brand.json."
            }
            Write-Warn 'a customer app needs a server address'
            continue
        }

        $addr = ConvertTo-ServerAddress $raw
        if ($null -eq $addr) {
            $msg = "'$raw' is not a server address - use http://host or https://host, with an optional :port."
            if ($NonInteractive -or $Server) { Stop-WithError $msg }
            Write-Warn $msg
            continue
        }

        if ($NoProbe) {
            Write-Warn "$addr - not probed (-NoProbe)"
        } else {
            $probe = Test-Server $addr
            if ($probe) {
                Write-Ok "$addr - $probe"
            } else {
                Write-Warn "$addr did not answer"
                if ($NonInteractive) { Stop-WithError "$addr did not answer. Check it, or pass -NoProbe to build anyway." }
                if (-not (Read-YesNo '  Build with it anyway?')) {
                    if ($Server) { Stop-WithError 'Stopped - check the -Server address.' }
                    continue
                }
            }
        }
        if ($addr.StartsWith('http://')) {
            Write-Warn 'plain HTTP - passwords cross the network unencrypted'
        }
        $b.Server = $addr
        break
    }
}

# ---------------------------------------------------------------------------
Write-Step 'Checking outputs'
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
foreach ($b in $Brands) {
    $name = "$($b.FileBase)-$BuildName"
    if ($Tag) { $name = "$name-$Tag" }
    $b | Add-Member -NotePropertyName ApkPath -NotePropertyValue (Join-Path $OutDir "$name.apk")
    if ((Test-Path $b.ApkPath) -and -not $Force) {
        if (-not $NonInteractive) {
            Write-Warn "$(Split-Path -Leaf $b.ApkPath) already exists (a build of this same version)"
            if (Read-YesNo '  Replace it?') { continue }
        }
        Stop-WithError ("$($b.ApkPath) already exists.`n`n" +
            "Bump 'version:' in pubspec.yaml (and lib\app\version.dart), pass -Tag, or -Force to replace it.")
    }
}
Write-Ok 'output names checked'

Push-Location $AppDir
try {
    $GitBranch = (& git rev-parse --abbrev-ref HEAD | Select-Object -First 1).Trim()
    $Commit    = (& git rev-parse --short HEAD | Select-Object -First 1).Trim()
    $Dirty     = @(& git status --porcelain -- .)
} finally { Pop-Location }
Write-Ok "building $GitBranch @ $Commit"
if ($Dirty.Count -gt 0) {
    Write-Warn "the app has $($Dirty.Count) uncommitted change(s) - they WILL be in this build"
    $Commit = "$Commit+uncommitted"
}

# ---------------------------------------------------------------------------
Write-Step 'Fetching packages'
Invoke-In $AppDir 'flutter pub get' { & flutter pub get }
Write-Ok 'packages ready'

if ($SkipTests) {
    Write-Warn 'tests skipped (-SkipTests) - do not ship this build'
} else {
    Write-Step 'Running the tests'
    Invoke-In $AppDir 'flutter test' { & flutter test }
    Write-Ok 'tests passed'
}

# ---------------------------------------------------------------------------
$Built = @()
foreach ($b in $Brands) {
    Write-Step "Building $($b.AppName) ($($b.Id))"
    $defines = "--dart-define-from-file=$($b.Defines)"
    $hostDef = "--dart-define=ORBIX_HOST=$($b.Server)"

    if (-not $SkipTests) {
        # The compiled brand must be exactly its profile - proves the defines reach the build.
        Invoke-In $AppDir "brand check ($($b.Id))" { & flutter test test/brand_test.dart $defines }
        Write-Ok 'brand profile compiles as written'
    }

    $apk = Join-Path $AppDir "build\app\outputs\flutter-apk\app-$($b.Id)-release.apk"
    if (Test-Path $apk) { Remove-Item -Force $apk }
    Invoke-In $AppDir "flutter build apk ($($b.Id))" {
        & flutter build apk --release --flavor $b.Id $defines $hostDef
    }
    if (-not (Test-Path $apk)) { Stop-WithError "The build reported success but $apk is missing." }

    # Signed with the release key - never the debug one.
    $certs = & $ApkSigner verify --print-certs $apk 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) { Stop-WithError "The APK signature does not verify:`n$certs" }
    if ($certs -match 'CN=Android Debug') { Stop-WithError 'The APK is signed with the DEBUG key. Check android\key.properties.' }
    $certMatch = [regex]::Match($certs, 'certificate SHA-256 digest:\s*([0-9a-fA-F]+)')
    $certSha = $(if ($certMatch.Success) { $certMatch.Groups[1].Value } else { '' })
    Write-Ok "signed with the release key ($($certSha.Substring(0, [math]::Min(16, $certSha.Length)))...)"

    Copy-Item $apk $b.ApkPath -Force
    $sha = (Get-FileHash -Algorithm SHA256 $b.ApkPath).Hash
    $record = [ordered]@{
        brand         = $b.Id
        appName       = $b.AppName
        company       = $b.Company
        applicationId = $b.AppId
        version       = $Version
        server        = $(if ($b.Server) { $b.Server } else { '(none - asks on first launch)' })
        commit        = $Commit
        branch        = $GitBranch
        builtAt       = (Get-Date).ToString('yyyy-MM-ddTHH:mm:sszzz')
        sha256        = $sha
        signingCert   = $certSha
    }
    $json = $record | ConvertTo-Json
    [IO.File]::WriteAllText(($b.ApkPath -replace '\.apk$', '.json'), $json + "`n", (New-Object System.Text.UTF8Encoding($false)))
    Write-Ok "$(Split-Path -Leaf $b.ApkPath)  ($([math]::Round((Get-Item $b.ApkPath).Length / 1MB, 1)) MB)"
    $Built += $b
}

# ---------------------------------------------------------------------------
Write-Step 'Done'
foreach ($b in $Built) {
    $srv = $(if ($b.Server) { $b.Server } else { '(asks on first launch)' })
    Write-Host ("  {0,-30} {1,-30} {2}" -f (Split-Path -Leaf $b.ApkPath), $b.AppId, $srv)
}
Write-Host ''
Write-Host "  in $OutDir  (version $Version, $Commit)"
Write-Host '  Each APK has a .json record beside it: brand, app id, server, commit, SHA-256.'
Write-Host ''
