# NFSSoundtrack Radio Bot

Java app that connects NFSSoundtrack Radio bot, which will play songs from the website's kind of database

If you encounter issues, please write to [help@nfssoundtrack.com](mailto:help@nfssoundtrack.com) or reach me via Discord or even website Disqus platform.

## Prepare

First, follow part of [JDA guide](https://github.com/DV8FromTheWorld/JDA/wiki/3%29-Getting-Started) to create your bot application, fetch id and token of bot.

Generate bot invitation:
"https://discord.com/api/oauth2/authorize?client_id=<YOUR_BOT_ID>&permissions=3148032&scope=bot"

Bot requires permissions for:
- ```Send messages (text permission)```
- ```Connect (voice permission)```
- ```Speak (voice permission)```
- ```Priority speaker (voice permission)```

Prepare (create) text channel and voice channel for the bot.

## Launch

Set values for following properties in default.properties file:
- ```voicechannel.id```
- ```textchannel.id```
- ```bot.id```
- ```bot.token```

Open command line, make sure default.properties location in the same location as JAR file and write in command line:

```sh
$ java -jar NFSSoundtrack-Radio-0.7.1.jar
```

Then visit [localhost:5000/start](http://localhost:5000/start) to start bot.

## Endpoints

- [localhost:5000/start](http://localhost:5000/start) - starts bot (if not yet connected to voice channel)
- [localhost:5000/stop](http://localhost:5000/stop) - stops bot and disconnects it from server
- [localhost:5000/radioon](http://localhost:5000/radioon) - enables radio mode (once playback finishes, bot will randomly select next song to play); radio mode is enabled by default
- [localhost:5000/radiooff](http://localhost:5000/radiooff) - disables radio mode (once playback finishes, bot will not play next random song)


## Properties file

There is a property file called default.properties, which bot will look for once deployed

Property file can have following properties:
### Obligatory properties
- ```voicechannel.id``` - id of voice channel, where bot should play songs
- ```textchannel.id``` - id of text channel, where bot should receive and send messages to trigger radio events
- ```bot.id``` - id of bot
- ```bot.token``` - token of bot

### Additional properties
- ```series.ids``` - list of series ids to filter separated by a comma, to see the mapping, please check file ```gamemapping.html```
- ```games.ids``` - list of game ids to filter separated by a comma, to see the mapping, please check file ```gamemapping.html```
- ```songs.ids``` - list of songs ids to filter separated by a comma
- ```queue.limit``` - limit of queue size, default is 10
- ```quality.level``` - audio quality level, either LOW, MEDIUM or HIGH, default is HIGH
- ```volume.start``` - volume level to be set for bot during launch, default is 100
- ```bot.token``` - bot's token required to make bot work (by default it's hidden in the file)
- ```stop.on.empty.channel``` - when no one is listening to radio, just stop playing music and wait for someone to join
- ```pingadmin.id``` - user id to write PM to when bot gets stuck with error 429

## Available commands

- ```~find [-title:"Born Too Slow"] [-band:"The Crystal Method"] [-game:"Need For Speed Underground"]``` - this will look for song with criteria that match provided title, band and/or game; command will provide NFSSoundtrack DB Info
- ```~findAndPlayFirst [-title:"Born Too Slow"] [-band:"The Crystal Method"] [-game:"Need For Speed Underground"]``` - find song, if at least one found, play first
- ```~findAndPlayAll [-title:"Born Too Slow"] [-band:"The Crystal Method"] [-game:"Need For Speed Underground"]``` - find song, if at least one found, add all to queue (number of songs added is driven by ```queue.limit``` property)
- ```~play [-id: 2135]``` - plays song with NFSSoundtrack id provided via find command
- ```~play [-youtube: https://www.youtube.com/watch?v=DoJQfzxu9r8]``` - plays song with YouTube id provided via find command
- ```~skip``` - skips current song and gets next song in a queue (permissions required)
- ```~random``` - gets song from NFSSoundtrack DB and adds it to queue
- ```~now``` - prints info about currently played song (with info from NFSSoundtrack when possible)
- ```~queue``` - prints info about songs in queue, you can set queue maximum size in properties file
- ```~clear``` - clears queue but currently played song keeps playing
- ```~setVolume [0-100]``` - sets bot volume between 0 and 100, you can set default volume in properties file