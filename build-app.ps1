$adbPath = "E:\Android\sdk\platform-tools\adb.exe"

cmd.exe /c 'gradlew.bat clean'
cmd.exe /c 'gradlew.bat assembleRelease'
write-host "The Last Exit Code is:" $LastExitCode

if($LastExitCode -eq 0) {

    New-Item -ItemType Directory -Force -Path Release

    $gatewayApk = ".\app\build\outputs\apk"

    $destpath = ".\Release"

    Remove-Item .\Release\*

    get-childitem -path $gatewayApk -Filter "TagLink-*.apk" |

        sort-object -Property LastWriteTime |
    
        select-object -last 1 | copy-item -Destination (join-path $destpath "taglink.apk")
		
}