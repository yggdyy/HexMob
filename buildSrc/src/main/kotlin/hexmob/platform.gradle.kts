// A convention plugin that should be applied to all subprojects corresponding to a specific modloader/platform, such as fabric and forge.

@file:Suppress("UnstableApiUsage")

package hexmob

import libs
import kotlin.io.path.div

plugins {
    id("hexmob.minecraft")
    id("hexmob.mod-publish")
    id("hexmob.utils.mod-dependencies")

    id("com.github.johnrengelman.shadow")
}

val platform: String by project
val curseforgeId: String by project
val modrinthId: String by project

val minecraftVersion = libs.versions.minecraft.get()

val platformCapitalized = platform.capitalize()

architectury {
    platformSetupLoomIde()
}

configurations {
    register("common")
    register("shadowCommon")
    compileClasspath {
        extendsFrom(get("common"))
    }
    runtimeClasspath {
        extendsFrom(get("common"))
    }
    // this needs to wait until Loom has been configured
    afterEvaluate {
        named("development$platformCapitalized") {
            extendsFrom(get("common"))
        }
    }
}

if (platform == "fabric") {
    // GeckoLib 4.8.2 (and architectury-injectables' `net.fabricmc:fabric-loader:+`
    // wildcard) drag fabric-loader up once those jars land in the shared Gradle
    // cache. The 1.20.1 stack's fabric-api here now resolves to 0.92.6+1.20.1 and
    // GeckoLib 4.8.2, which both hard-require fabric-loader >= 0.16.10.
    //
    // fabric-loader 0.16.10 itself needs a sponge-mixin with the JAVA_22
    // CompatibilityLevel (mixin 0.8.7), but plain resolution leaves in the old
    // 1.20.1-era sponge-mixin 0.12.5 (no JAVA_22) -> NoSuchFieldError on boot.
    // Pin both so the stack stays coherent and the `+` wildcards can't float a
    // 1.21-era loader on top of old mixin again.
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "net.fabricmc" && requested.name == "fabric-loader") {
                useVersion("0.16.10")
            }
            if (requested.group == "net.fabricmc" && requested.name == "sponge-mixin") {
                useVersion("0.15.2+mixin.0.8.7")
            }
        }
    }
}

dependencies {
    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProduction$platformCapitalized")) { isTransitive = false }
}

sourceSets {
    main {
        resources {
            source(project(":common").sourceSets.main.get().resources)
        }
    }
}

tasks {
    val ciArtifacts = register<Copy>("ciArtifacts") {
        from(remapJar)
        into(rootDir.toPath() / "build" / "ciArtifacts")
    }

    build {
        dependsOn(ciArtifacts)
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev-shadow"
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile = shadowJar.get().archiveFile
        archiveClassifier = null
    }

    jar {
        archiveClassifier = "dev"
    }

    kotlinSourcesJar {
        val commonSources = project(":common").tasks.kotlinSourcesJar
        dependsOn(commonSources)
        from(commonSources.flatMap { it.archiveFile }.map(::zipTree))
    }
}

publishMods {
    file = tasks.remapJar.flatMap { it.archiveFile }

    modLoaders.add(platform)

    displayName = modLoaders.map { values ->
        val loaders = values.joinToString(", ") { it.capitalize() }
        // CurseForge/Modrinth version display name (eg. "v0.1.0 [Fabric, Quilt]")
        "v${project.version} [$loaders]"
    }

    curseforge {
        accessToken = System.getenv("CURSEFORGE_TOKEN") ?: ""
        projectId = curseforgeId
        minecraftVersions.add(minecraftVersion)
        // TODO: update if your mod is only client-side or server-side!
        clientRequired = true
        serverRequired = true
    }

    modrinth {
        accessToken = System.getenv("MODRINTH_TOKEN") ?: ""
        projectId = modrinthId
        minecraftVersions.add(minecraftVersion)
    }

    github {
        parent(rootProject.tasks.named("publishGithub"))
    }
}

fun String.capitalize() = replaceFirstChar(Char::uppercase)
