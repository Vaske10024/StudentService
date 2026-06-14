$ErrorActionPreference = 'Stop'

if (-not $env:JAVA_HOME) {
    $bundledJdk = Join-Path $HOME '.jdks\ms-11.0.30'
    if (Test-Path $bundledJdk) {
        $env:JAVA_HOME = $bundledJdk
    }
}

if (-not $env:JAVA_HOME) {
    throw 'JAVA_HOME must point to JDK 11 before running the live E2E suite.'
}

$backendRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..\..\ServerAndDTOs')
$maven = Join-Path $backendRoot 'mvnw.cmd'
$rootPom = Join-Path $backendRoot 'pom.xml'
Set-Location $backendRoot

& $maven -q -f $rootPom -Plive-e2e -pl server -am install -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $maven -q -f $rootPom -Plive-e2e -pl server spring-boot:run '-Dspring-boot.run.profiles=e2e' '-Dspring-boot.run.arguments=--server.port=8081'
exit $LASTEXITCODE
