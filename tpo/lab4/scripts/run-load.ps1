param(
    [Parameter(Mandatory = $true)]
    [ValidateSet(1, 2, 3)]
    [int]$Config,

    [string]$JMeterBat = ""
)

$ErrorActionPreference = "Stop"

function Resolve-JMeter {
    param([string]$ProvidedPath)

    if ($ProvidedPath -and (Test-Path -LiteralPath $ProvidedPath)) {
        return (Resolve-Path -LiteralPath $ProvidedPath).Path
    }

    if ($env:JMETER_HOME) {
        $candidate = Join-Path $env:JMETER_HOME "bin\jmeter.bat"
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    $cmd = Get-Command jmeter -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }

    throw "JMeter was not found. Install Apache JMeter, set JMETER_HOME, add apache-jmeter\bin to PATH, or pass -JMeterBat C:\path\to\jmeter.bat."
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$jmeter = Resolve-JMeter $JMeterBat
$resultDir = Join-Path $root "load\result"
$htmlDir = Join-Path $root "load\html-report-config$Config"
$resultFile = Join-Path $resultDir "config$Config.csv"
$testPlan = Join-Path $root "load\test-plan.jmx"
$userProps = Join-Path $root "user.properties"

New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
if (Test-Path -LiteralPath $htmlDir) {
    Remove-Item -Recurse -Force -LiteralPath $htmlDir
}
if (Test-Path -LiteralPath $resultFile) {
    Remove-Item -Force -LiteralPath $resultFile
}

Write-Host "Running load test for config=$Config"
& $jmeter -n -t $testPlan -q $userProps -l $resultFile -e -o $htmlDir "-Jtarget_config=$Config"
