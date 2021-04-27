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

// ===[ DISCORD ]===
include(":discord:common")
include(":discord:gateway")
include(":discord:commands")
include(":discord:interactions")