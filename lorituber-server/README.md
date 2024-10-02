# Loritta's LoriTuber Game Server

This is Loritta's Lorituber Game Server, used to simulate the entire LoriTuber game world.

> From https://www.youtube.com/watch?v=scS3f_YSYO0
> "Simulator effect: players imagine your world, your simulation, is more detailed, more rich, more complicated than it actually is, don’t talk them out of it. Part of exploring a world is uncovering these loops of complexity and covert rules. Part of learning the rules of the game are actually embedded in the game." -Will Wright

## Why not keep the state 100% on SQLite instead of periodically synchronizing to the disk?

Because SQLite is slower than having the objects in memory, because when querying from SQLite, you still need to convert the retrieved data to something the JVM can understand.

## Why not use SQLite in-memory and then periodically dump the data to the disk?

It has the same issue as above.

Also fun fact: Using SQLite in disk is actually FASTER when you have multiple readers, because the in-memory SQLite database does not allow parallel connections.

## Why the tables don't use `IdTable`?

Using auto increment + primary key on a SQLite database is slooow, even in operations like batch inserts when all the rows are already present on the database (so, no inserts, just replaces).

So, as a workaround, we use `UNIQUE INDEX` for the IDs, this is WAY faster! Batch inserting 1 million rows with a `UNIQUE INDEX` is ~4 seconds, while with a `PRIMARY KEY AUTO INCREMENT` is 13s!