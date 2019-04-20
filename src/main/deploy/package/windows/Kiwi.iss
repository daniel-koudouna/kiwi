;This file will be executed next to the application bundle image
;I.e. current directory will contain folder Kiwi with application files
[Setup]
AppId={{PRODUCT_APP_IDENTIFIER}}
AppName=APPLICATION_NAME
AppVersion=APPLICATION_VERSION
AppVerName=APPLICATION_NAME APPLICATION_VERSION
AppPublisher=APPLICATION_VENDOR
AppComments=APPLICATION_COMMENTS
AppCopyright=APPLICATION_COPYRIGHT
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
ChangesAssociations=Yes
DefaultDirName={pf}\APPLICATION_NAME
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=No
DefaultGroupName=APPLICATION_GROUP
;Optional License
LicenseFile=APPLICATION_LICENSE_FILE
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=INSTALLER_FILE_NAME
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile=APPLICATION_NAME\APPLICATION_NAME.ico
UninstallDisplayIcon={app}\APPLICATION_NAME.ico
UninstallDisplayName=APPLICATION_NAME
UsePreviousAppDir=No
WizardImageStretch=No
WizardSmallImageFile=APPLICATION_NAME-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=ARCHITECTURE_BIT_MODE



[Tasks]
Name: Association; Description: "Associate image file extensions ('.jpg','.png')"; GroupDescription: "File extensions"

[Registry]
Root: HKCR; Subkey: ".jpg"; ValueData: "APPLICATION_NAME"; Flags: uninsdeletevalue; ValueType: string;  ValueName: "" ; Tasks: Association
Root: HKCR; Subkey: ".png"; ValueData: "APPLICATION_NAME"; Flags: uninsdeletevalue; ValueType: string;  ValueName: "" ; Tasks: Association
Root: HKCR; Subkey: "APPLICATION_NAME"; ValueData: "Kiwi Compatible File"; Flags: uninsdeletekey; ValueType: string;  ValueName: ""
;;; Root: HKCR; Subkey: "APPLICATION_NAME\DefaultIcon"; ValueData: "{app}\APPLICATION_NAME.exe,0"; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "APPLICATION_NAME\shell\open\command"; ValueData: """{app}\APPLICATION_NAME.exe"" ""%1""";  ValueType: string;  ValueName: ""

Root: HKCR; Subkey: "Applications\APPLICATION_NAME.exe"; ValueData: "Kiwi - A Lightweight Image Viewer";  ValueType: string;  ValueName: "FriendlyAppName"

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
Source: "APPLICATION_NAME\APPLICATION_NAME.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "APPLICATION_NAME\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: program;

[Icons]
Name: "{group}\APPLICATION_NAME"; Filename: "{app}\APPLICATION_NAME.exe"; IconFilename: "{app}\APPLICATION_NAME.ico"; Check: APPLICATION_MENU_SHORTCUT()
Name: "{commondesktop}\APPLICATION_NAME"; Filename: "{app}\APPLICATION_NAME.exe"; IconFilename: "{app}\APPLICATION_NAME.ico"; Check: APPLICATION_DESKTOP_SHORTCUT()

[Run]
Filename: "{app}\RUN_FILENAME.exe"; Parameters: "-Xappcds:generatecache"; Check: APPLICATION_APP_CDS_INSTALL()
Filename: "{app}\RUN_FILENAME.exe"; Description: "{cm:LaunchProgram,APPLICATION_NAME}"; Flags: nowait postinstall skipifsilent; Check: APPLICATION_NOT_SERVICE()
Filename: "{app}\RUN_FILENAME.exe"; Parameters: "-install -svcName ""APPLICATION_NAME"" -svcDesc ""APPLICATION_DESCRIPTION"" -mainExe ""APPLICATION_LAUNCHER_FILENAME"" START_ON_INSTALL RUN_AT_STARTUP"; Check: APPLICATION_SERVICE()

[UninstallRun]
Filename: "{app}\RUN_FILENAME.exe "; Parameters: "-uninstall -svcName APPLICATION_NAME STOP_ON_UNINSTALL"; Check: APPLICATION_SERVICE()

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
