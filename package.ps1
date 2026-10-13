<#
.SYNOPSIS
    Construit l'archive de remise du projet en excluant tous les artefacts
    locaux (base de données, compilation, dépendances, outillage).

.DESCRIPTION
    Le .gitignore n'a aucun effet sur une archive : `Compress-Archive -Path *`
    empaquette littéralement le contenu du dossier, y compris :

      .mysql/              ~208 Mo  données brutes de la base + clés privées MySQL
      backend/target/       ~59 Mo  classes compilées et dépendances Maven
      tools/                        Maven complet (binaire versionné ailleurs)
      frontend/node_modules/         dépendances npm reconstituables par npm ci
      frontend/dist/                 build de production reproductible
      .env                           secrets locaux (le .env.example, lui, part)
      *.pem *.key *.p12 *.jks …      clés privées : jamais livrées

    La vérification finale applique les MÊMES règles que l'inventaire et refuse
    toute entrée `.env*` sauf le modèle `.env.example`.

    Ce script lit la liste des fichiers un par un, filtre, et écrit l'archive
    via System.IO.Compression : rien n'est copié dans un dossier temporaire.

    Ensuite, il rouvre l'archive produite et VERIFIE qu'aucun chemin interdit
    n'y figure — une exclusion qu'on croit faite et qui ne l'est pas, c'est
    exactement ce qui a fait échouer la remise précédente.

.PARAMETER Destination
    Chemin de l'archive produite. Par défaut : à côté du dossier du projet
    (jamais dedans, pour ne jamais s'inclure soi-même).

.PARAMETER Lister
    Affiche ce qui serait inclus/exclus, sans rien écrire.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\package.ps1
    powershell -ExecutionPolicy Bypass -File .\package.ps1 -Lister
#>
[CmdletBinding()]
param(
    [string]$Destination,
    [switch]$Lister
)

$ErrorActionPreference = 'Stop'
$racine = $PSScriptRoot

if (-not $Destination) {
    $Destination = Join-Path (Split-Path $racine -Parent) 'portefeuille-initiatives.zip'
}

# --- Exclusions --------------------------------------------------------------
# Préfixes de dossier, relatifs à la racine, séparateur '/' (forme normalisée).
$dossiersExclus = @(
    '.mysql/'
    'backend/target/'
    'tools/'
    'frontend/node_modules/'
    'frontend/dist/'
    'frontend/build/'
    '.git/'
    '.mvn/'
    '.idea/'
    '.vscode/'
)

# Noms de fichier exacts, quel que soit le dossier.
$fichiersExclus = @(
    '.DS_Store'
    'Thumbs.db'
)

# --- Secrets -----------------------------------------------------------------
# Rien de ce qui suit ne doit jamais quater l'archive de remise, quel que soit
# le chemin où il se trouve. Cette liste s'applique AUSSI à la vérification
# post-construction : une exclusion déclarée mais non contrôlée, c'est exactement
# le genre d'oubli qui a fait échouer une remise précédente.
$extensionsSecretes = @(
    '.pem'       # clés/certificats MySQL générés par database\init-mysql.ps1
    '.key'       # clé privée brute
    '.p12', '.pfx'   # paquets de certificats
    '.jks', '.keystore'  # keystores Java
    '.ppk', '.der'
)
$nomsSecretes = @('id_rsa', 'id_dsa', 'id_ecdsa', 'id_ed25519')

function Test-Secret {
    param([string]$rel)

    $nom = ($rel -split '/')[-1]

    # `.env`, `.env.local`, `.env.production`… : tout est refusé, sauf le
    # MODÈLE .env.example, qui ne contient aucune valeur réelle et que le
    # destinataire a besoin de trouver.
    if ($nom -like '.env*' -and $nom -ne '.env.example') { return $true }

    if ($nomsSecretes -contains $nom) { return $true }

    $ext = [System.IO.Path]::GetExtension($nom)
    if ($ext -and $extensionsSecretes -contains $ext.ToLowerInvariant()) { return $true }

    return $false
}

function Test-Exclu {
    param([string]$rel)

    foreach ($d in $dossiersExclus) {
        if ($rel.StartsWith($d, [StringComparison]::OrdinalIgnoreCase)) { return $true }
    }

    $nom = ($rel -split '/')[-1]

    if ($fichiersExclus -contains $nom) { return $true }
    if (Test-Secret $rel) { return $true }
    if ($nom -like '*.log') { return $true }
    if ($nom -like '*.class') { return $true }
    if ($nom -like '*.iml') { return $true }

    # Jamais d'archive dans l'archive : évite l'auto-inclusion si la
    # destination a été placée à l'intérieur du dossier du projet.
    if ($nom -like '*.zip') { return $true }

    return $false
}

# --- Inventaire --------------------------------------------------------------
$inclus = New-Object System.Collections.Generic.List[object]
$exclus = @{}

foreach ($f in Get-ChildItem -LiteralPath $racine -Recurse -File -Force) {
    $rel = $f.FullName.Substring($racine.Length + 1).Replace('\', '/')

    if (Test-Exclu $rel) {
        # On agrège par dossier de premier niveau pour un affichage lisible.
        $racineExclusion = ($rel -split '/')[0]
        if (-not $exclus.ContainsKey($racineExclusion)) { $exclus[$racineExclusion] = @{ n = 0; o = 0L } }
        $exclus[$racineExclusion].n++
        $exclus[$racineExclusion].o += $f.Length
    }
    else {
        $inclus.Add([pscustomobject]@{ Rel = $rel; Fichier = $f })
    }
}

$octetsInclus = 0L
foreach ($e in $inclus) { $octetsInclus += $e.Fichier.Length }

Write-Output "Racine      : $racine"
Write-Output ("Inclus      : {0} fichier(s), {1:N1} Mo" -f $inclus.Count, ($octetsInclus / 1MB))
Write-Output ''
Write-Output 'Exclus :'
foreach ($k in ($exclus.Keys | Sort-Object)) {
    Write-Output ("  {0,-24} {1,6} fichier(s)  {2,8:N1} Mo" -f $k, $exclus[$k].n, ($exclus[$k].o / 1MB))
}

if ($Lister) { return }

# --- Construction ------------------------------------------------------------
if (Test-Path -LiteralPath $Destination) { Remove-Item -LiteralPath $Destination -Force }
$destinationDir = Split-Path $Destination -Parent
if (-not (Test-Path -LiteralPath $destinationDir)) {
    New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
}

Add-Type -AssemblyName System.IO.Compression | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null

$flot = [System.IO.File]::Open($Destination, [System.IO.FileMode]::CreateNew)
$archive = New-Object System.IO.Compression.ZipArchive(
    $flot, [System.IO.Compression.ZipArchiveMode]::Create)

try {
    foreach ($e in $inclus) {
        $entree = $archive.CreateEntry($e.Rel, [System.IO.Compression.CompressionLevel]::Optimal)
        if ($e.Fichier.LastWriteTime -gt [DateTime]'1980-01-01') {
            $entree.LastWriteTime = $e.Fichier.LastWriteTime
        }
        $source = [System.IO.File]::OpenRead($e.Fichier.FullName)
        try {
            $cible = $entree.Open()
            try { $source.CopyTo($cible) } finally { $cible.Dispose() }
        }
        finally { $source.Dispose() }
    }
}
finally {
    $archive.Dispose()
    $flot.Dispose()
}

# --- Vérification : on rouvre ce qu'on vient de produire ---------------------
$verif = [System.IO.Compression.ZipFile]::OpenRead($Destination)
try {
    $entrees = @($verif.Entries)
}
finally { $verif.Dispose() }

# On réutilise EXACTEMENT les mêmes règles que l'inventaire : une règle qui
# décide de l'inclusion doit être la même que celle qui vérifie l'exclusion.
$violations = @($entrees | Where-Object {
        Test-Exclu ($_.FullName.Replace('\', '/'))
    })

$poids = (Get-Item -LiteralPath $Destination).Length

Write-Output ''
Write-Output ("Archive    : {0}" -f $Destination)
Write-Output ("Taille     : {0:N1} Mo, {1} entrée(s)" -f ($poids / 1MB), $entrees.Count)

if ($violations.Count -gt 0) {
    Write-Output ''
    Write-Output "ECHEC — chemins interdits présents dans l'archive :"
    $violations | Select-Object -First 20 | ForEach-Object { Write-Output ("  " + $_.FullName) }
    exit 1
}

Write-Output 'Vérification : OK — aucun chemin exclu présent.'
exit 0
