#Invoke a Powershell script on a remote host from a Windows monitoring station.
param (
    [string]$remoteHost = $(throw "-remoteHost is required."),
    [string]$username = $(throw "-username is required."),
    [string]$password = $(throw "-password is required.")
 )

# PSCredential to be used in Invoke-Command
$securePw = ConvertTo-SecureString $password -AsPlainText -Force
$psCred = New-Object System.Management.Automation.PSCredential($username,$securePw)

# Create a new session with the Credential
$session = New-PSSession -ComputerName $remoteHost -Credential $psCred

# Each plugin creates its folder inside uptime/plugins/java folder.
$pluginName = "exchange2013-mailbox-server-monitor"
# Find uptime folder in Program Files by using MIBDIRS environment variable.
$MIBDIRS_path = $env:MIBDIRS
$uptimeLocation = $MIBDIRS_path.Substring(0,$MIBDIRS_path.Length-4)
# File path of scripts directory in uptime folder.
$scriptLocation = "$uptimeLocation/plugins/java/$pluginName/scripts"

Invoke-Command -Session $session -FilePath "$scriptLocation/exchange2013-mailbox-server.ps1"