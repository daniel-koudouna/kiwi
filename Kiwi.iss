;This file will be executed next to the application bundle image
;I.e. current directory will contain folder Kiwi with application files
#define name "Kiwi"
#define res "src\com\proxy\kiwi\res"

[Setup]
AlwaysShowComponentsList=Yes
AppId={{fxApplication}}
AppName="{#name}"
AppVersion=1.0
AppVerName=Kiwi
AppPublisher=Daniel Koudouna
AppComments=kiwi
AppCopyright=Copyright (C) 2016
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
ChangesAssociations=Yes
DefaultDirName="{pf}\{#name}"
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=No
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=No
DefaultGroupName=kiwi
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1
OutputDir=dist
OutputBaseFilename="{#name}-setup"
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile={#res}\Kiwi.ico
UninstallDisplayIcon={app}\Kiwi.ico
UninstallDisplayName=Kiwi
WizardImageStretch=No
WizardSmallImageFile={#res}\Kiwi-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64

[Tasks]
Name: Association; Description: "Associate image file extensions ('.jpg','.png')"; GroupDescription: "File extensions"

[Registry]
Root: HKCR; Subkey: ".jpg"; ValueData: "{#name}"; Flags: uninsdeletevalue; ValueType: string;  ValueName: "" ; Tasks: Association
Root: HKCR; Subkey: ".png"; ValueData: "{#name}"; Flags: uninsdeletevalue; ValueType: string;  ValueName: "" ; Tasks: Association
Root: HKCR; Subkey: "{#name}"; ValueData: "{#name} Compatible File"; Flags: uninsdeletekey; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "{#name}\DefaultIcon"; ValueData: "{app}\{#name}.exe,0"; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "{#name}\shell\open\command"; ValueData: """{app}\{#name}.exe"" ""%1""";  ValueType: string;  ValueName: ""

Root: HKCR; Subkey: "Applications\{#name}.exe"; ValueData: "Kiwi - A Lightweight Image Viewer";  ValueType: string;  ValueName: "FriendlyAppName"

[Types]
Name: "full"; Description: "Full installation"
Name: "compact"; Description: "Compact installation"
Name: "custom"; Description: "Custom installation"; Flags: iscustom

[Components]
Name: "program"; Description: "Program Files"; Types: full compact custom; Flags: fixed
Name: "group"; Description: "Add Start Menu Entry"; Types: full;
Name: "icon"; Description: "Desktop Shortcut"; Types: full;


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "dist\kiwi.exe"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: program;
Source: "{#res}\Kiwi.ico"; DestDir: "{app}"; Components: program;

[Icons]
Name: "{group}\Kiwi"; Filename: "{app}\Kiwi.exe"; IconFilename: "{app}\Kiwi.ico"; Components: group;
Name: "{commondesktop}\Kiwi"; Filename: "{app}\Kiwi.exe";  IconFilename: "{app}\Kiwi.ico"; Components: icon;


[Run]
Filename: "{app}\Kiwi.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\Kiwi.exe"; Description: "{cm:LaunchProgram,Kiwi}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\Kiwi.exe"; Parameters: "-install -svcName ""Kiwi"" -svcDesc ""Kiwi"" -mainExe ""Kiwi.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\Kiwi.exe "; Parameters: "-uninstall -svcName Kiwi -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
