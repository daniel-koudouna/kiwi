;This file will be executed next to the application bundle image
;I.e. current directory will contain folder Kiwi with application files

#define name "@name@"
#define version "@version@"
#define description "@description@"
#define author "@author"

[Setup]
AppId={#name}
AppName={#name}
AppVersion={#version}
AppVerName={#name} {#version}
AppPublisher={#author}
AppCopyright=Copyright (C) 2019
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
ChangesAssociations=Yes
DefaultDirName={pf}\{#name}
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=No
DefaultGroupName={#name}
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename={#name}-{#version}-setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile={#name}.ico
UninstallDisplayIcon={app}\{#name}.ico
UninstallDisplayName={#name}
UsePreviousAppDir=No
WizardImageStretch=No
WizardImageFile={#name}-left.bmp
WizardSmallImageFile={#name}-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64



[Tasks]
Name: Association; Description: "Associate image file extensions ('.jpg','.png')"; GroupDescription: "File extensions"
Name: OpenWith; Description: "Add 'Open with Kiwi' to Explorer Context Menu"; GroupDescription: "File extensions"

[Registry]
Root: HKCR; Subkey: ".jpg"; ValueData: "{#name}"; Flags: uninsdeletevalue; ValueType: string;  ValueName: "" ; Tasks: Association
Root: HKCR; Subkey: ".png"; ValueData: "{#name}"; Flags: uninsdeletevalue; ValueType: string;  ValueName: "" ; Tasks: Association
Root: HKCR; Subkey: "{#name}"; ValueData: "{#name} Compatible File"; Flags: uninsdeletekey; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "{#name}\DefaultIcon"; ValueData: "{app}\{#name}.exe,0"; ValueType: string;  ValueName: ""

Root: HKCR; Subkey: "Directory\shell\{#name}"; ValueData: "Open with {#name}"; Flags: uninsdeletekey; ValueType: string;  ValueName: ""; Tasks: OpenWith
Root: HKCR; Subkey: "Directory\shell\{#name}\command"; ValueData: """{app}\{#name}.exe"" ""%L"""; Flags: uninsdeletekey; ValueType: string;  ValueName: ""; Tasks: OpenWith
Root: HKCR; Subkey: "Directory\shell\{#name}"; ValueData: "{app}\{#name}.exe,0"; Flags: uninsdeletekey; ValueType: string;  ValueName: "Icon"; Tasks: OpenWith

Root: HKCR; Subkey: "{#name}\shell\open\command"; ValueData: """{app}\{#name}.exe"" ""%1""";  ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "Applications\{#name}.exe"; ValueData: {#description};  ValueType: string;  ValueName: "FriendlyAppName"

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
Source: "{#name}\{#name}.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#name}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: program;

[Icons]
Name: "{group}\{#name}"; Filename: "{app}\{#name}.exe"; IconFilename: "{app}\{#name}.ico"; Components: group;
Name: "{commondesktop}\{#name}"; Filename: "{app}\{#name}.exe"; IconFilename: "{app}\{#name}.ico"; Components: icon;

[Run]
Filename: "{app}\{#name}.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\{#name}.exe"; Description: "{cm:LaunchProgram,{#name}}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\{#name}.exe"; Parameters: "-install -svcName ""{#name}"" -svcDesc ""{#description}"" -mainExe ""{#name}.exe"" "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\{#name}.exe "; Parameters: "-uninstall -svcName {#name} -stopOnUninstall"; Check: returnFalse()

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
