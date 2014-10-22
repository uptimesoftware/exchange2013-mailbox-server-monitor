#Invoke a Powershell script on a remote host from a Windows monitoring station.
param (
    [string]$remoteHost = $(throw "-remoteHost is required."),
    [string]$username = $(throw "-username is required."),
    [string]$password = $(throw "-password is required.")
 )

# PSCredential to be used in Invoke-Command
$securePw = ConvertTo-SecureString $password -AsPlainText -Force
$psCred = New-Object System.Management.Automation.PSCredential($username,$securePw)

#Create a new session with the Credential
$session = New-PSSession -ComputerName $remoteHost -Credential $psCred

Invoke-Command -Session $session -FilePath "C:\uptime_scripts\exchange2013-mailbox-server.ps1"