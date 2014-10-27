### Windows Monitoring Station

###### Allow Powershell Remoting on the Remotehost (Exchange Mailbox Server)
Run Powershell 2.0 on the Remotehost and run the following Cmdlet.
> Enable-PSRemoting

This command starts the WinRM service if it’s not allready started and sets the startup type to automatic. Adds firewall exceptions for WS-Management communications and creates a listener to accept requests.

###### Add Trusted Hosts on the Localcomputer (Windows up.time Monitoring Station)
On the Local Computer run Powershell and run the following Cmdlet. This allows you to connect to any host. It also starts WinRM if its not already started.
> Set-Item WSMan:\localhost\Client\TrustedHosts *

After that you may have to restart the WinRM service
> Restart-Service winrm -Force

Commands from: http://www.thomasmaurer.ch/2011/01/quick-powershell-remoting-guide/

### Linux Monitoring Station 
* Install an up.time Agent on a target Windows remote host.
* Click 'Advanced option', then click 'Custom Scripts'.
* Enter the follwing in the 'Command Name' field.

> ex2013mailbox

* Enter the following in the 'Path to script' field.

> cmd /c "cscript "C:\Program Files (x86)\uptime software\up.time agent\scripts\exchange2013-mailbox-server.vbs" //Nologo //T:0"

* Click Add/Edit and close and restart up.time Agent.
* Copy and paste 'exchange2013-mailbox-server.vbs' file from 'https://github.com/uptimesoftware/exchange2013-mailbox-server-monitor/tree/master/src/scripts' to 'C:\Program Files (x86)\uptime software\up.time agent\scripts' folder.
