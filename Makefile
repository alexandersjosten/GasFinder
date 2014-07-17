
debug:
	 ant debug
install:
	adb uninstall sjosten.android
	adb install bin/GasFinder-debug.apk
start:
	adb shell am start -n sjosten.android/.StartActivity
	adb logcat

unlock:
	adb shell input keyevent 26
#	sleep 0.5
#	adb shell input text "pin"
#	adb shell input keyevent 66
	

all:debug install start
	
