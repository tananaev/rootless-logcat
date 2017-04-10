# Android Logcat without Root

[![Get it on Google Play](http://www.tananaev.com/badges/google-play.svg)](https://play.google.com/store/apps/details?id=com.tananaev.logcat) [![Get it on F-Droid](http://www.tananaev.com/badges/f-droid.svg)](https://f-droid.org/repository/browse/?fdid=com.tananaev.logcat)

Read Android logs without root access. The app uses remote debugging to connect to phone local ADB daemon. Configuring remote debugging on a phone can be challenging and requires some technical expertise. It has to be done once and after each device reboot.

Enable USB debugging first on your device. Navigate to Settings > About Phone and tap Build number seven times. Go back, access the Developer options menu and check USB debugging option.

Next step is to enable remote debugging. You need to have Android SDK installed on your computer. Connect your phone via USB cable and run following adb command:

```
adb tcpip 5555
```

Disconnect USB cable before trying to use the app. Some phones have problems handling network ADB connection when they are connected via USB as well.

Sometimes establishing connection to ADB hangs and requires killing and restarting the app. It seems to be a problem with ADB daemon itself and not the app issue.

## Contacts

Author - Anton Tananaev ([anton.tananaev@gmail.com](mailto:anton.tananaev@gmail.com))

## License

    Apache License, Version 2.0

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
