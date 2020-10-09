# flutter_play_asset
This library provides the play asset delivery functionality from the play core library for Flutter framework. Only works for Android.

## Installation & Testing
### Setup Android Environtment settings
- Open the project build.gradle, then update the android build tool to version 4.0.0 or higher
- Save your asset pack in your directory by following this guideline https://developer.android.com/guide/app-bundle/asset-delivery/build-native-java
- Import this package by updating your dependencies
```yaml
dependencies:
  flutter_play_asset: ^1.0.0
```

### How it works
#### Initialize the listener
You need to initialize an object FlutterPlayAsset inside the class where the download take action. You can handle the download process by overriding the callback method that will be called by the download process
```dart
class ViewPlayAsset {
  void OnProgressDownload(int percentage){

  }

  void OnAssetPathFound(String path){

  }

  void OnProcessLoadingAssetPath(String path){

  }
}
```

The class that requesting the download need to implement ViewPlayAsset to be able to override the callbacks. So you need to initialize your FlutterPlayAsset in the initState
```dart
FlutterPlayAsset.init(this);
```

#### Check location of the downloaded asset pack
You can use the assets inside on-demand asset pack by load them from the internal storage. So you need the absolute path where the asset pack is saved, then append them with the asset name. FlutterPlayAsset a method returning this path with this function.
```dart
flutterPlayAsset.getAssetPath(assetPackName);
```

#### Testing the app
You can test the app locally by downloading the bundletool and following this guideline https://developer.android.com/guide/app-bundle/asset-delivery#next-step-instructions