# Vonage module for Appcelerator Titanium

## Requirements

* Titanium SDK 9+
* Vonage <small>(formerly OpenTok)</small> account

## API

### Properties
* apiKey
* sessionId
* token

### Methods
* connect
* disconnect

### Events
* ready
* disconnected
* streamReceived
* streamDropped
* sessionError
* streamCreated
* streamDestroyed
* error

## Example

```xml
<modules>
    <module platform="android">ti.vonage</module>
</modules>
```

index.xml
```xml
<Alloy>
	<Window onOpen="onOpen">
		<VonageView module="ti.vonage" id="vonage"/>
		<TextField hintText="api key" id="tf_api" value=""/>
		<TextField hintText="session id" id="tf_session" value=""/>
		<TextField hintText="token" id="tf_token" value=""/>
		<Button id="btn" title="connect" onClick="onClickConnect"/>
        <Button id="btn" title="disconnect" onClick="onClickDisconnect"/>
	</Window>
</Alloy>

```

index.js
```javascript
$.index.open();

function onOpen(e) {
	$.vonage.initialize();
}

$.vonage.addEventListener("ready", function() {
	console.log("ready");
})

function onClickConnect(e) {
	$.vonage.apiKey = $.tf_api.value;
	$.vonage.sessionId = $.tf_session.value;
	$.vonage.token = $.tf_token.value;

	$.vonage.connect();
}

function onClickDisconnect(e) {
	$.vonage.disconnect();
}
```

index.tss
```
".container" : {
	backgroundColor: "white"
}
"TextField" : {
	borderWidth: 1,
	borderColor: "#000",
	left: 10,
	right: 10,
	backgroundColor: "transparent",
	color: "#000"
}
"#vonage" : {
	height: 400,
	width: Ti.UI.FILL,
	top: 0
}
```

build.gradle
```
repositories {
	google()
	jcenter()
	maven { url 'https://tokbox.bintray.com/maven' }
}

dependencies {
	implementation 'com.opentok.android:opentok-android-sdk:2.16.6'
	implementation 'pub.devrel:easypermissions:3.0.0'
}
```
