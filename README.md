# FlickLyticsN

SOEN 6441 Play application for exploring TMDb media data with reactive updates.

## Current branch highlights

- Reactive WebSockets for live search updates.
- Actor-based supervision for task handling and failure recovery.
- Dedicated actor flow for the Reviews feature.
- Movie/TV details, global diversity, financial performance, and person stats pages.

## Reviews task

The Reviews flow is handled asynchronously through `HomeController`, `SupervisorActor`, and `ReviewsActor`, with tests covering the actor path and controller behavior.

## Reviews-related files changed

- `app/actors/ReviewsActor.java`
- `app/actors/SupervisorActor.java`
- `app/modules/ActorModule.java`
- `app/controllers/HomeController.java`
- `test/actors/ReviewsActorTest.java`
- `test/actors/SupervisorActorTest.java`
- `test/controllers/HomeControllerTest.java`

## Running tests

The project has been validated with Java 17. From the project root, run:

```zsh
cd /Users/naomi/FlickLytics/FlickLyticsN
export JAVA_HOME="$('/usr/libexec/java_home' -v 17)"
export PATH="$JAVA_HOME/bin:$PATH"
sbt test
```
# SOEN 6441 project
curl -i \
-H "Connection: Upgrade" \
-H "Upgrade: websocket" \
-H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
-H "Sec-WebSocket-Version: 13" \
http://localhost:9000/ws/tv
- in browser test:

http://localhost:9000/flicklytics/movie/550
# Output of test:
  [BroadcastHub] pushing id=550 type=movie title=Fight Club
  [MediaDetailsActor] received id=550 type=movie filter=movie matches=true
  [MediaDetailsActor] forwarded id=550 to WebSocket
  [MediaDetailsActor] received id=550 type=movie filter=tv matches=false
  
- how to config 21 jdk for jacoco

$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
java -version
javac -version
sbt clean test jacoco
