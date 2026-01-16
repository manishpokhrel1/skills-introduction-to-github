<#
Run unit tests inside a Gradle Docker image. Requires Docker Desktop.
Usage: Open PowerShell at repo root and run: .\run-tests.ps1
#>
$pwd = (Get-Location).Path
Write-Host "Running tests using project Gradle wrapper if present, else Docker"
if (Test-Path -Path "$pwd\gradlew.bat") {
	Write-Host "Found gradlew.bat - running local wrapper"
	& "$pwd\gradlew.bat" test
} else {
	Write-Host "gradlew not found - falling back to Dockerized Gradle"
	docker run --rm -v "${pwd}:/workspace" -w /workspace gradle:8.3-jdk17 gradle test --no-daemon --stacktrace
}