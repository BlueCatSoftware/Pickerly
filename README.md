# Pickerly (Image Picker Library)

Pickerly is a simple android library which allows you to pick image seamlessly in your android
project

## How to get it

Step 1. Add the JitPack repository in your root build.gradle at the end of repositories:

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency

```
	dependencies {
                implementation 'com.github.BlueCatSoftware:Pickerly:Tag'
	}
```

Latest
Version: [![](https://jitpack.io/v/BlueCatSoftware/Pickerly.svg)](https://jitpack.io/#BlueCatSoftware/Pickerly)
## Features

- Ability to single select
- Ability to multi select images
- Compatible with android 10 and above

# Requirements

- Androidx
- Glide Library
- For Android 10 add `android:requestLegacyExternalStorage = "true"`

## Usage

``` java
//Declaration
Pickerly pick = new Pickerly();

//Whether to enable transparency
pick.enableTransparency(true);

//For Dynamic Height
  pick.enableHeight(false);
// Set height if it's enable
pick.setHeightPercent(400);

//Toggling Multiple Selection On Or Off
pick.enableMultiSelect(false); 

//Listeners after selecting
pick.setItemListener(new Pickerly.selectListener() {

   @Override
   public void onItemSelected(String item) {
      //Do something with the selected file path
     }
   });

//Or if you enable Multiple Selection use this listener

pick.setItemListener(new Pickerly.multiSelectListener() {
   @Override
   public void onMultiItemSelected(String[] items) {
       //Do something with the arrays of selected paths		
     }
   });

//Finally, show the imagePickerDialog
pick.show(getSupportFragmentManager(), "0");
```


## Special Thanks
- Ilyasse Salama
- khaled-0
