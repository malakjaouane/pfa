# ============================================================
# Démarre le serveur MySQL local (port 3306) - données dans .mysql/data
# Usage : powershell -NoProfile -ExecutionPolicy Bypass -File database\start-mysql.ps1
# Prérequis : exécuter une fois database\init-mysql.ps1 pour initialiser le datadir.
# ============================================================
param(
    [int]$Port = 3306,
    [int]$TimeoutSeconds = 30
)

$ErrorActionPreference = 'Stop'

$base    = Split-Path -Parent $PSScriptRoot
$mysqlbin = 'C:\Program Files\MySQL\MySQL Server 8.4\bin'
$mysqld  = Join-Path $mysqlbin 'mysqld.exe'
$basedir = 'C:\Program Files\MySQL\MySQL Server 8.4'
$datadir = Join-Path $base '.mysql\data'
$logdir  = Join-Path $base '.mysql'

if (-not (Test-Path $mysqld)) {
    Write-Error "mysqld introuvable ($mysqld). Installez MySQL 8.4 ou adaptez le chemin."
    exit 1
}
if (-not (Test-Path $datadir)) {
    Write-Error "Datadir absent ($datadir). Exécutez d'abord database\init-mysql.ps1."
    exit 1
}
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
    Write-Output "MySQL écoute déjà sur le port $Port."
    exit 0
}

New-Item -ItemType Directory -Force -Path $logdir | Out-Null

# IMPORTANT : chaque argument contenant un espace doit être explicitement
# entouré de guillemets. Sinon Start-Process tronque le chemin au premier
# espace (ex. "C:\Program Files\..." devient "C:\Program") et mysqld ne peut
# plus charger ses plugins.
$arguments = @(
    "--basedir=`"$basedir`"",
    "--datadir=`"$datadir`"",
    "--port=$Port",
    '--bind-address=127.0.0.1'
)

Start-Process -FilePath $mysqld -ArgumentList $arguments -WindowStyle Hidden `
    -RedirectStandardError (Join-Path $logdir 'mysqld.err.log') `
    -RedirectStandardOutput (Join-Path $logdir 'mysqld.out.log') | Out-Null

# Attente active : MySQL n'accepte pas de connexion immédiatement après le lancement.
for ($i = 1; $i -le $TimeoutSeconds; $i++) {
    Start-Sleep -Seconds 1
    if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
        Write-Output "MySQL démarré et à l'écoute sur 127.0.0.1:$Port."
        exit 0
    }
}

Write-Error "MySQL n'a pas démarré dans les $TimeoutSeconds s. Consultez .mysql\mysqld.err.log."
exit 1
