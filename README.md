# SOEN 6441 project
curl -i \
-H "Connection: Upgrade" \
-H "Upgrade: websocket" \
-H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
-H "Sec-WebSocket-Version: 13" \
http://localhost:9000/ws/tv
- in browser test:
# windows intellij setting
curl.exe -v `
  -H "Upgrade: websocket" `
-H "Connection: Upgrade" `
  -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" `
-H "Sec-WebSocket-Version: 13" `
http://localhost:9000/mediaDetailsWs?mediaType=tv

- test on windows
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