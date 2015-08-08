# owntracks-receiver

This is Harald's owntracks receiver playground. It's probably not useful to anyone
else yet...

## TODO

- store configuration in the database.
- configuration of topics, etc. via web page.
- all activity last 24 hours
- show points / waypoints on google maps

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.
It's also useful to have [Owntracks][2] installed on a mobile device,
and an MQTT broker if you want control over your messaging.


[1]: https://github.com/technomancy/leiningen
[2]: http://owntracks.org/

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2015 Harald Koch <harald.koch@gmail.com>. All Rights Reserved.
