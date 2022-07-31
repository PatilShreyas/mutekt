# Mutekt
(Pronunciation: _**/mjuːˈteɪt/**_, 'k' is silent).   
Generates mutable models from immutable model definitions. _Made with ❤️ for Kotliners_.

[![Build](https://github.com/PatilShreyas/mutekt/actions/workflows/build.yml/badge.svg)](https://github.com/PatilShreyas/mutekt/actions/workflows/build.yml)
[![Release](https://github.com/PatilShreyas/mutekt/actions/workflows/release.yml/badge.svg)](https://github.com/PatilShreyas/mutekt/actions/workflows/release.yml)
[![GitHub](https://img.shields.io/github/license/PatilShreyas/mutekt?label=License)](LICENSE)

[![Github Followers](https://img.shields.io/github/followers/PatilShreyas?label=Follow&style=social)](https://github.com/PatilShreyas)
[![GitHub stars](https://img.shields.io/github/stars/PatilShreyas/mutekt?style=social)](https://github.com/PatilShreyas/mutekt/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/PatilShreyas/mutekt?style=social)](https://github.com/PatilShreyas/mutekt/network/members)
[![GitHub watchers](https://img.shields.io/github/watchers/PatilShreyas/mutekt?style=social)](https://github.com/PatilShreyas/mutekt/watchers)
[![Twitter Follow](https://img.shields.io/twitter/follow/imShreyasPatil?label=Follow&style=social)](https://twitter.com/imShreyasPatil)

## Motivation

// TODO

## Usage

You can check [/example](/example) directory which includes example application for demonstration.

### 1. Gradle setup

#### 1.1 Add dependencies

In `build.gradle` of app module, include this dependency

```gradle
dependencies {
    // TODO
}
```

_You can find latest version and changelogs in the [releases](https://github.com/PatilShreyas/mutekt/releases)_.

#### 1.2 Include generated classes in sources

// TODO: JVM Projects  
// TODO: Android Projects

### 2. Apply annotation

// TODO: Write steps for applying annotation

```kotlin
@GenerateMutableModel
interface NotesState {
    val isLoading: Boolean
    val notes: List<String>
    val error: String?
}
```

// TODO: Write about building project so KSP will do magic

### 3. Use generated mutable model

// TODO: Use MutableXXX for accessing mutable model.  
// TODO: Use asStateFlow() on MutableXXX model to get read-only (immutable) model instance of interface created above.

```kotlin
class NotesViewModel: ViewModel() {

    /**
     * Instance of mutable model [MutableNotesState] which is generated with Mutekt.
     */
    private val _state = MutableNotesState(isLoading = false, notes = emptyList(), error = null)

    /**
     * Immutable StateFlow of a [NotesState].
     * Whenever any property of [_state] is changed, this StateFlow is updated with new value of immutable model.
     */
    val state: StateFlow<NotesState> = _state.asStateFlow()

    fun loadNotes() {
        viewModelScope.launch {
            _state.isLoading = true

            try {
                _state.notes = getNotes()
            } catch (e: Throwable) {
                _state.error = e.message ?: "Error occurred"
            }
            _state.isLoading = false
        }
    }
}
```

---

## 🧐 How _Mutekt_ works?

// TODO

---

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
