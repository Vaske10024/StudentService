param([Parameter(Mandatory=$true)][string]$InputFile,[switch]$ConfirmRestore)
if (-not $ConfirmRestore) { throw "Restore requires -ConfirmRestore." }
if (-not (Test-Path -LiteralPath $InputFile)) { throw "Backup file does not exist." }
$required = @('DB_HOST','DB_PORT','DB_NAME','DB_USER','DB_PASSWORD')
foreach ($name in $required) { if (-not (Get-Item "Env:$name" -ErrorAction SilentlyContinue)) { throw "Missing environment variable $name" } }
$env:MYSQL_PWD = $env:DB_PASSWORD
Get-Content -LiteralPath $InputFile -Raw | mysql -h $env:DB_HOST -P $env:DB_PORT -u $env:DB_USER $env:DB_NAME
if ($LASTEXITCODE -ne 0) { throw "Restore failed." }
