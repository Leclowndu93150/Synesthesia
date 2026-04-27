plugins {
    id("dev.prism")
}

group = "com.leclowndu93150"
version = "1.1.0"

prism {
    curseMaven()

    metadata {
        modId = "synesthesia"
        name = "Synesthesia: Sound Visualizer"
        description = "Addon to Extreme Sound Muffler that allows you to locate sound sources (or a static position where sounds were played) around you."
        license = "MIT"
        author("Leclowndu93150")
    }

    publishing {
        changelogFile = "CHANGELOG.md"
        type = STABLE

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1402473"
        }
    }

    version("1.18.2") {
        forge {
            loaderVersion = "40.3.10"
            loaderVersionRange = "[40,)"
            dependencies {
                modImplementation("curse.maven:extreme-sound-muffler-363363:4033047")
            }
            publishingDependencies {
                requires("extreme-sound-muffler")
            }
        }
    }

    version("1.20.1") {
        common {
            modCompileOnly("curse.maven:extreme-sound-muffler-363363:7469697")
        }
        fabric {
            loaderVersion = "0.16.10"
            fabricApi("0.92.6+1.20.1")
            dependencies {
                modImplementation("curse.maven:extreme-sound-muffler-fabric-official-566140:7469698")
                implementation("me.zeroeightsix:fiber:0.23.0-2")
            }
            publishingDependencies {
                requires("fabric-api")
                requires("extreme-sound-muffler-fabric-official")
            }
        }
        forge {
            loaderVersion = "47.4.0"
            loaderVersionRange = "[47,)"
            dependencies {
                modImplementation("curse.maven:extreme-sound-muffler-363363:7469697")
            }
            publishingDependencies {
                requires("extreme-sound-muffler")
            }
        }
    }

    version("1.21.1") {
        common {
            compileOnly("curse.maven:extreme-sound-muffler-363363:7895926")
        }
        fabric {
            loaderVersion = "0.16.10"
            fabricApi("0.116.4+1.21.1")
            dependencies {
                modImplementation("curse.maven:extreme-sound-muffler-fabric-official-566140:7895927")
                implementation("me.zeroeightsix:fiber:0.23.0-2")
            }
            publishingDependencies {
                requires("fabric-api")
                requires("extreme-sound-muffler-fabric-official")
            }
        }
        neoforge {
            loaderVersion = "21.1.216"
            loaderVersionRange = "[4,)"
            dependencies {
                implementation("curse.maven:extreme-sound-muffler-363363:7895926")
            }
            publishingDependencies {
                requires("extreme-sound-muffler")
            }
        }
    }

    version("26.1.2") {
        common {
            compileOnly("curse.maven:extreme-sound-muffler-363363:7910513")
        }
        fabric {
            loaderVersion = "0.17.2"
            fabricApi("0.146.0+26.1.2")
            dependencies {
                modImplementation("curse.maven:extreme-sound-muffler-fabric-official-566140:7910515")
            }
            publishingDependencies {
                requires("fabric-api")
                requires("extreme-sound-muffler-fabric-official")
            }
        }
        neoforge {
            loaderVersion = "26.1.2.12-beta"
            loaderVersionRange = "[4,)"
            dependencies {
                implementation("curse.maven:extreme-sound-muffler-363363:7910513")
            }
            publishingDependencies {
                requires("extreme-sound-muffler")
            }
        }
    }
}
