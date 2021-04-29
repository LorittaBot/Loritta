rootProject.name = "loritta-parent"

include(":common")

include(":services:memory")
include(":services:pudding")

// ===[ COMMANDS ]===
include(":commands")

// ===[ SERVICES ]===
include(":services:memory")
include(":services:pudding")

// ===[ CLI ]===
include(":cli")

//// ===[ PLATFORMS ]===

// ===[ CLI ]===
include(":platforms:cli")

// ===[ DISCORD ]===
include(":platforms:discord:common")
include(":platforms:discord:gateway")
include(":platforms:discord:commands")
include(":platforms:discord:interactions")

// ===[ TWITTER ]===
include(":platforms:twitter")