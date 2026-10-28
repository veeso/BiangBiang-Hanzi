android_java_home := "/Applications/Android Studio.app/Contents/jbr/Contents/Home"
ios_project := "ios/BiangBiang Hanzi.xcodeproj"
ios_scheme := "BiangBiang Hanzi"
ios_destination := "platform=iOS Simulator,name=iPhone 16"

# Build the iOS app.
[group('build')]
build_ios args="":
  xcodebuild -project {{ quote(ios_project) }} -scheme {{ quote(ios_scheme) }} build {{ args }}

# Run the iOS unit tests.
[group('build')]
test_ios args="":
  xcodebuild test -project {{ quote(ios_project) }} -scheme {{ quote(ios_scheme) }} -destination {{ quote(ios_destination) }} {{ args }}

# Build the Android app in debug mode.
[group('build')]
build_android args="":
  cd android && JAVA_HOME={{ quote(android_java_home) }} PATH="{{ android_java_home }}/bin:$PATH" ./gradlew assembleDebug {{ args }}

# Run the Android unit tests.
[group('build')]
test_android args="":
  cd android && JAVA_HOME={{ quote(android_java_home) }} PATH="{{ android_java_home }}/bin:$PATH" ./gradlew test {{ args }}

# Build the website CSS.
[group('build')]
build_site:
  npx tailwindcss@3 -i ./site/input.css -o ./site/output.css --minify

# Build both apps.
[group('build')]
build:
  just build_ios
  just build_android

# Run the unit tests of both apps.
[group('build')]
test:
  just test_ios
  just test_android
