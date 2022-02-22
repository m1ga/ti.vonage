# Vonage module for Appcelerator Titanium

<img src="images/vonage.jpg"/>

<br/>
<br/>

<span class="badge-buymeacoffee"><a href="https://www.buymeacoffee.com/miga" title="donate"><img src="https://img.shields.io/badge/buy%20me%20a%20coke-donate-orange.svg" alt="Buy Me A Coke donate button" /></a></span>

## Requirements

* Titanium SDK 9+
* Vonage <small>(formerly OpenTok)</small> account

## API

### Properties
* apiKey
* sessionId
* token
* audioOnly (creation only)

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

## How to use it

Listen to the `streamReceived` event. It will return a `view` with the videos. You'll add those views to your normal Ti app. The `type` and `streamId` will help you to e.g. remove them later again if a participant will disconnect.

## Example

```xml
<modules>
    <module platform="android">ti.vonage</module>
</modules>
```

```javascript
var vonage = require("ti.vonage");

function onOpen(e) {
	vonage.initialize();
}

vonage.addEventListener("ready", function() {
	console.log("ready");
})

vonage.addEventListener("streamReceived", function(e) {
  // view with the camera stream:
	var v = Ti.UI.createView({height:190,	width: 190})
	v.add(e.view);

	console.log(e.type, e.streamId);
})
vonage.addEventListener("streamDropped", function(e) {
	console.log(e.type, e.streamId);
})

function onClickConnect(e) {
	vonage.apiKey = $.tf_api.value;
	vonage.sessionId = $.tf_session.value;
	vonage.token = $.tf_token.value;

	vonage.connect();
}

function onClickDisconnect(e) {
	vonage.disconnect();
}
```

build.gradle
```
repositories {
	google()
	jcenter()
	mavenCentral()
}
```
