rootProject.name = "loritta-parent"

include(":common")

include(":in-memory-services")

// ===[ COMMANDS ]===
include(":commands")

// ===[ CLI ]===
include(":cli")

// ===[ DISCORD ]===
include(":discord:common")
include(":discord:gateway")
include(":discord:commands")
include(":discord:interactions")