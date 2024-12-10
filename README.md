## Material MediaPicker

### Support:
- Pick image, video from gallery
- Single, multi select mode
- Support fast scroll
- Support drag selection
- Support preview media

### Install:
<pre>
Step 1. Add the JitPack repository to your build file

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

Step 2. Add the dependency
dependencies {
    implementation 'com.github.MCT-LIB:MediaPicker:{latest-version}'
}
</pre>

### Use:
<pre>
MediaPicker.pick(getSupportFragmentManager(), new MediaPickerOption.Builder()
    /* mode */
    .single(uri -> {})
    .multi(uris -> {})
    .multiExact(uris -> {}, 3)
    .multiRange(uris -> {}, 1, 10)

    /* type */
    .image()
    .video()
    .all()

    /* theme...etc */
    .themeStrategy(M3ThemeStrategy.DEFAULT)
    .themeStrategy(M3ThemeStrategy.INHERIT)
    .themeStrategy(M3ThemeStrategy.DYNAMIC)

    /* build */
    .build()
);
</pre>
