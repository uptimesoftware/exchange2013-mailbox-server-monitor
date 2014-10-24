exchange2013-mailbox-server-monitor
===================================

For internal testing :

a) ( Only for a Windows monitoring station ) Enable Powershell remoting :
  - Run cmd as admin then run Powershell.exe on a target remote host.
  - Execute 'Enable-PSRemoting'.
  - Follow the instructions on cmd. (Typical yes-to-all steps.)
  - Execute 'Set-Item wsman:localhost\client\trustedhosts -value *', so that all the local computers are trusted.

b) ( Only for a Linux monitoring station ) : 
 - Install an up.time Agent on a target Windows remote host.
 - Click 'Advanced option', then click 'Custom Scripts'.
 - Type ex2013mailbox in 'Command Name:' field.
 - Type cmd /c "cscript "C:\uptime_scripts\exchange2013-mailbox-server.vbs" //Nologo //T:0" in 'Path to script:' field.
 - Click Add/Edit and close and restart up.time Agent.

(feel free to add more details)
