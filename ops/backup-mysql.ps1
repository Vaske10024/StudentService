param([Parameter(Mandatory=$true)][string]$OutputFile)
$required = @('DB_HOST','DB_PORT','DB_NAME','DB_USER','DB_PASSWORD')
foreach ($name in $required) { if (-not (Get-Item "Env:$name" -ErrorAction SilentlyContinue)) { throw "Missing environment variable $name" } }
$env:MYSQL_PWD = $env:DB_PASSWORD
mysqldump --single-transaction --routines --triggers -h $env:DB_HOST -P $env:DB_PORT -u $env:DB_USER $env:DB_NAME --result-file=$OutputFile
if ($LASTEXITCODE -ne 0) { throw "Backup failed." }
