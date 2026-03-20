import java.net.URI

plugins {
    id("hexmob.minecraft")
}

architectury {
    common("fabric", "forge")
}

repositories {
    maven {
        name = "GeckoLib"
        url = URI.create("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven {
        name = "Modrinth"
        url = URI.create("https://api.modrinth.com/maven")
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(kotlin("reflect"))

    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury)

    modApi(libs.hexcasting.common)

    modApi(libs.clothConfig.common)
    //modApi("maven.modrinth:geckolib:aVW7Z5da")

    libs.mixinExtras.common.also {
        implementation(it)
        annotationProcessor(it)
    }

    modImplementation("software.bernie.geckolib:geckolib-fabric-1.20.1:4.8.2")
    modImplementation("com.eliotlash.mclib:mclib:20")
}
