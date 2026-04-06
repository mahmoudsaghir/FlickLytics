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