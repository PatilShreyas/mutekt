<h1 align="center">Mutekt</h1>

<p align="center">(Pronunciation: <b><i>/mjuːˈteɪt/</i></b>, 'k' is silent)</p>

<h3 align="center"><i>"Simplify mutating "immutable" state models"</i></h3>

<p align="center">
    <a href="https://github.com/PatilShreyas/mutekt/actions/workflows/build.yml"><img src="https://github.com/PatilShreyas/mutekt/actions/workflows/build.yml/badge.svg"/></a>
    <a href="https://github.com/PatilShreyas/mutekt/actions/workflows/release.yml"><img src="https://github.com/PatilShreyas/mutekt/actions/workflows/release.yml/badge.svg"/></a>
    <a href="https://search.maven.org/search?q=g:dev.shreyaspatil.mutekt"><img src="https://img.shields.io/maven-central/v/dev.shreyaspatil.mutekt/mutekt-codegen?label=Maven%20Central&logo=android&style=flat-square"/></a>
    <a href="LICENSE"><img src="https://img.shields.io/github/license/PatilShreyas/mutekt?label=License)"/></a>
    <a href="https://codecov.io/gh/PatilShreyas/mutekt"><img src="https://codecov.io/gh/PatilShreyas/mutekt/branch/main/graph/badge.svg?token=t5722h7jWn"/></a>
</p>

Generates mutable models from immutable model definitions. It's based on Kotlin's Symbol Processor (KSP).
This is inspired from the concept _Redux_ and _Immer_ from JS world that let you write simpler immutable update logic 
using "mutating" syntax which helps simplify most reducer implementations. 
**So you just need to focus on actual development and _Mutekt_ will write boilerplate for you!** 😎

<p align="center">Like this ⬇️️</p>

![Mutekt Usage Example](mutekt-usage.gif)

## Usage

Try out the [example app](/example) to see it in action.

### 1. Apply annotation and generate model

Declare a state model as an `interface` and apply `@GenerateMutableModel` annotation to it.

Example:

```kotlin
@GenerateMutableModel
interface NotesState {
    val isLoading: Boolean
    val notes: List<String>
    val error: String?
}
// You can also apply annotation `@Immutable` if using for Jetpack Compose UI model.
```

Once done, **🔨Build project** and mutable model will be generated for the immutable definition by KSP.

### 2. Simply mutate and get immutable state

The mutable model can be created with the factory function which is generated with the name of an interface with prefix
`Mutable`.
_For example, if interface name is `ExampleState` then method name for creating mutable model will be
`MutableExampleState()` and will have parameters in it which are declared as public properties in the interface._

```kotlin
/**
 * Instance of mutable model [MutableNotesState] which is generated with Mutekt.
 */
private val _state = MutableNotesState(isLoading = true, notes = emptyList(), error = null)

fun setLoading() {
    _state.isLoading = true
}

fun setNotes() {
    _state.apply {
        isLoading = false
        notes = listOf("Lorem Ipsum")
    }
}
```

### 3. Getting reactive immutable value updates

To get immutable instance with reactive state updates, use method `asStateFlow()` which returns instance of
[`StateFlow<T>`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/).
Whenever any field of Mutable model is updated with new value, this StateFlow gets updated with new immutable state value.

```kotlin
val state: StateFlow<NotesState> = _state.asStateFlow()
```

#### Properties of immutable instance implemented by Mutekt:

- [x] Immutable model implementation promises to be truly ***Immutable*** i.e. once instance is created, its properties
will never change.
- [x] Implementation is actually a ***data class*** under the hood i.e. having `equals()` and `hashCode()` 
already overridden.

---

## Setting up _Mutekt_ in the project

### 1. Gradle setup

#### 1.1 Enable KSP in module

In order to support code generation at compile time, [enable KSP support in the module](https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project).

```groovy
plugins {
    id 'com.google.devtools.ksp' version '1.7.10-1.0.6'
}
```

#### 1.2 Add dependencies

In `build.gradle` of app module, include this dependency

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.shreyaspatil.mutekt:mutekt-core:$mutektVersion")
    ksp("dev.shreyaspatil.mutekt:mutekt-codegen:$mutektVersion")
    
    // Include kotlin coroutine to support usage of StateFlow 
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
```

_You can find the latest version and changelogs in the [releases](https://github.com/PatilShreyas/mutekt/releases)_.

#### 1.3 Include generated classes in sources

> **Note**   
> In order to make IDE aware of generated code, it's important to include KSP generated sources in the project source sets.

Include generated sources as follows:

<details open>
  <summary><b>Gradle (Groovy)</b></summary>

```groovy
kotlin {
    sourceSets {
        main.kotlin.srcDirs += 'build/generated/ksp/main/kotlin'
        test.kotlin.srcDirs += 'build/generated/ksp/test/kotlin'
    }
}
```

</details>

<details>
  <summary><b>Gradle (KTS)</b></summary>

```kotlin
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
```

</details>

<details>
  <summary><b>Android (Gradle - Groovy)</b></summary>

```groovy
android {
    applicationVariants.all { variant ->
        kotlin.sourceSets {
            def name = variant.name
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
```
</details>

<details>
  <summary><b>Android (Gradle - KTS)</b></summary>

```kotlin
android {
    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
```
</details>

## See also

- [Why Mutekt?](https://github.com/PatilShreyas/mutekt/wiki/Why-Mutekt%3F)
- [Generated code with Mutekt](https://github.com/PatilShreyas/mutekt/wiki/Code-generation-with-Mutekt)

## 👨‍💻 Development

Clone this repository and import in IntelliJ IDEA (_any edition_) or Android Studio.

### Module details

- `mutekt-core`: Contain core annotation and interface for mutekt
- `mutekt-codegen`: Includes sources for generating mutekt code with KSP
- `example`: Example application which demonstrates usage of this library.

### Verify build

- To verify whether project building or not: `./gradlew build`.
- To verify code formatting: `./gradlew spotlessCheck`.
- To reformat code with Spotless: `./gradlew spotlessApply`.

## 🙋‍♂️ Contribute 

Read [contribution guidelines](CONTRIBUTING.md) for more information regarding contribution.

## 💬 Discuss

Have any questions, doubts or want to present your opinions, views? You're always welcome. You can [start discussions](https://github.com/PatilShreyas/mutekt/discussions).

## 📝 License

```
Copyright 2022 Shreyas Patil

Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
