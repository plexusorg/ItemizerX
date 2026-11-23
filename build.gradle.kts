import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
}

group = "dev.plex"
version = "3.0.0"

// Build with -PpaperApiVersion=1.21.11-R0.1-SNAPSHOT to check against the oldest supported API.
val paperApiVersion = providers.gradleProperty("paperApiVersion")
    .getOrElse("26.3.build.+")

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }


}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

// Every permission ItemizerX checks. Each command checks only its own node; a group node such as
// itemizer.lore grants the nodes under it. docs/commands.md explains the packs below.
val permissionGroups = mapOf(
    "itemizer.lore" to listOf("add", "remove", "change", "clear"),
    "itemizer.potion" to listOf("add", "remove", "change", "color", "list"),
    "itemizer.attr" to listOf("add", "edit", "remove", "list", "listall"),
    "itemizer.flag" to listOf("add", "remove", "list", "listall"),
    "itemizer.enchant" to listOf("add", "remove", "list", "listall"),
)
// Each pack grants the pack before it and the nodes it lists. itemizer.* grants everything, which adds
// the nodes a player could use to run commands as others, build any item, or forge other plugins' data.
val permissionPacks = linkedMapOf(
    "itemizer.pack.cosmetic" to ("Change how items and signs look, and read item details" to listOf(
        "itemizer.use", "itemizer.name", "itemizer.lore", "itemizer.title", "itemizer.author", "itemizer.head",
        "itemizer.flag", "itemizer.sign", "itemizer.clearall", "itemizer.component", "itemizer.enchant.list",
        "itemizer.enchant.listall", "itemizer.attr.list", "itemizer.attr.listall", "itemizer.potion.list")),
    "itemizer.pack.gameplay" to ("Make items more powerful: enchantments, attributes, effects, properties" to listOf(
        "itemizer.pack.cosmetic", "itemizer.enchant", "itemizer.attr", "itemizer.potion", "itemizer.property",
        "itemizer.component.edit", "itemizer.data")),
    "itemizer.pack.creation" to ("Make, copy, and stack any item" to listOf(
        "itemizer.pack.gameplay", "itemizer.id", "itemizer.amount", "itemizer.container", "itemizer.preset",
        "itemizer.gui")),
)
val permissionNodes = linkedMapOf(
    "itemizer.use" to "Use /itemizer, undo, and redo",
    "itemizer.name" to "Rename items",
    "itemizer.id" to "Change an item's material",
    "itemizer.amount" to "Change the stack amount",
    "itemizer.property" to "Edit item properties",
    "itemizer.component" to "Show item data components",
    "itemizer.component.edit" to "Edit food, consumable, equippable, cooldown, attack range, and tool components, and reset components",
    "itemizer.component.raw" to "Apply raw components and replace the item with any /give item",
    "itemizer.data" to "Edit persistent data in the itemizerx namespace",
    "itemizer.data.foreign" to "Edit persistent data in other plugins' namespaces",
    "itemizer.container" to "Edit bundle and container contents",
    "itemizer.preset" to "Save, load, import, and export item presets",
    "itemizer.gui" to "Open the dialog and inventory editors",
    "itemizer.lore" to "Every lore command",
    "itemizer.lore.add" to "Add lore lines",
    "itemizer.lore.remove" to "Remove lore lines",
    "itemizer.lore.change" to "Change lore lines",
    "itemizer.lore.clear" to "Clear lore",
    "itemizer.potion" to "Every potion command",
    "itemizer.potion.add" to "Add or change potion effects",
    "itemizer.potion.remove" to "Remove potion effects",
    "itemizer.potion.change" to "Change the potion type",
    "itemizer.potion.color" to "Set the potion color",
    "itemizer.potion.list" to "List potion effects",
    "itemizer.attr" to "Every attribute command",
    "itemizer.attr.add" to "Add attribute modifiers",
    "itemizer.attr.edit" to "Edit attribute modifiers",
    "itemizer.attr.remove" to "Remove attribute modifiers",
    "itemizer.attr.list" to "List an item's attribute modifiers",
    "itemizer.attr.listall" to "List every attribute",
    "itemizer.flag" to "Every flag command",
    "itemizer.flag.add" to "Add item flags",
    "itemizer.flag.remove" to "Remove item flags",
    "itemizer.flag.list" to "List an item's flags",
    "itemizer.flag.listall" to "List every item flag",
    "itemizer.enchant" to "Every enchantment command",
    "itemizer.enchant.add" to "Add or change enchantments",
    "itemizer.enchant.remove" to "Remove enchantments",
    "itemizer.enchant.list" to "List an item's enchantments",
    "itemizer.enchant.listall" to "List every enchantment",
    "itemizer.title" to "Set a book's title",
    "itemizer.author" to "Set a book's author",
    "itemizer.head" to "Set the player of a head",
    "itemizer.sign" to "Change the text of a sign",
    "itemizer.sign.interactive" to "Put click events, selectors, scores, and NBT on signs",
    "itemizer.clearall" to "Clear all of an item's metadata",
)

paper {
    name = "ItemizerX"
    version = rootProject.version.toString()
    description = "A new way to edit your items"
    authors = listOf("Focusvity", "Telesphoreo")
    main = "dev.plex.itemizerx.ItemizerX"
    apiVersion = "1.21.11"
    foliaSupported = true

    permissions {
        permissionNodes.forEach { (node, text) ->
            register(node) {
                description = text
                children = permissionGroups[node].orEmpty().map { "$node.$it" }
            }
        }
        register("itemizer.*") {
            description = "Every ItemizerX permission"
            children = permissionNodes.keys.toList() + permissionPacks.keys
        }
        permissionPacks.forEach { (pack, contents) ->
            register(pack) {
                description = contents.first
                children = contents.second
            }
        }
    }
}

// Paper never published a downloadable 26.1 server build; 26.1.1 was the first 26.1.x build.
val testedVersions = listOf("1.21.11", "26.1.1", "26.1.2", "26.2", "26.3")
tasks {
    testedVersions.forEach {
        register<RunServer>("runServer-$it") {
            minecraftVersion(it)
            pluginJars(shadowJar.flatMap { jar -> jar.archiveFile })
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$it"))
        }
    }

    assemble {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "dev.plex.metrics")
    }

    jar {
        archiveClassifier.set("unshaded")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}
