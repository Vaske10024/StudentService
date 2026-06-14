$ErrorActionPreference = 'Stop'
$env:VITE_API_BASE_URL = 'http://127.0.0.1:8081'
npm run dev -- --host 127.0.0.1 --port 5174
exit $LASTEXITCODE
