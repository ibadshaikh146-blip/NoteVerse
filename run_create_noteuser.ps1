param(
    [string]$RootPassword
)

$sqlFile = Join-Path $PSScriptRoot 'create_noteuser.sql'
if (-not (Test-Path $sqlFile)) {
    Write-Error "SQL file not found: $sqlFile"
    exit 1
}

$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlCmd) {
    Write-Error "mysql client not found. Install MySQL client or add it to PATH."
    exit 1
}

if (-not $RootPassword) {
    $secure = Read-Host -Prompt "Enter root password" -AsSecureString
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        $RootPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

$cmd = "mysql --user=root --password=\"$RootPassword\" --host=127.0.0.1 --port=3307 < \"$sqlFile\""
Write-Output "Executing SQL file against MySQL (you may be prompted by mysql):"
Write-Output $cmd

# Use cmd.exe to allow input redirection
& cmd.exe /c $cmd
$code = $LASTEXITCODE
if ($code -eq 0) {
    Write-Output "SQL executed successfully."
} else {
    Write-Error "mysql exited with code $code"
}
exit $code
