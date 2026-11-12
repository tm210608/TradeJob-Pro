$ErrorActionPreference = 'Stop'
$node = Join-Path $env:ProgramFiles 'nodejs\node.exe'
& $node (Join-Path $PSScriptRoot 'run-android-tests.cjs')
exit $LASTEXITCODE
