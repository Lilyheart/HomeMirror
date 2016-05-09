# HomeMirror

HomeMirror Project written by [Lily](https://github.com/lilyheart) and [Lee](https://github.com/lgladsden2053) for a CIS111B Project under [Dr. Martin](https://github.com/kmartinphd).

To Use this fork
===
Create a keys.xml file in the res/values folder.  Use the following code as a template and fill in codes as neccessary.

dark_sky_api_key value can be obtained from [here](https://developer.forecast.io/).
Flic ids can be obtained from [here](https://flic.io/partners/developers/credentials).
Withing id information can be obtained from [here](http://oauth.withings.com/api/doc).

```XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="dark_sky_api_key">Your Key</string>
    <string name="flic_appID">Your Key</string>
    <string name="flic_appSecret">Your Key</string>
    <string name="flic_appName">Your Name</string>

    <string name="withings_userid">Your userID</string>
    <string name="withings_con_key">Your conkey</string>
    <string name="withings_sig">Your Sig</string>
    <string name="withings_sig_method">Your Method (probably HMAC-SHA1)</string>
    <string name="withings_oauthtoken">Your Token</string>
    <string name="withings_oauthvers">1.0</string>
</resources>
```



Links from the original HomeMirror
===

[The original HomeMirror](https://github.com/HannahMitt/HomeMirror)

The Android APP of the original project
[Google Play Link](https://play.google.com/store/apps/details?id=com.morristaedt.mirror)


At the heart of this project, is 'put a mirror on it'. [Check out alternative mirror projects and feel free to add your own reflections](https://github.com/HannahMitt/HomeMirror/wiki/Other-mirror-projects-with-alternate-technologies)
