const { withDangerousMod, withAppBuildGradle, withSettingsGradle } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

/**
 * Expo config plugin to integrate AndroidLiquidGlass native module
 */
const withLiquidGlass = (config) => {
  // First, modify build.gradle
  config = withAppBuildGradle(config, (config) => {
    const buildGradlePath = config.modRequest.platformProjectRoot + '/app/build.gradle';
    let buildGradle = fs.readFileSync(buildGradlePath, 'utf8');
    
    // Add Compose plugin if not present
    if (!buildGradle.includes('id("org.jetbrains.kotlin.plugin.compose")')) {
      const pluginsMatch = buildGradle.match(/plugins\s*\{/);
      if (pluginsMatch) {
        const insertIndex = pluginsMatch.index + pluginsMatch[0].length;
        buildGradle = 
          buildGradle.slice(0, insertIndex) + 
          '\n        id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"' + 
          buildGradle.slice(insertIndex);
      }
    }
    
    // Add buildFeatures.compose if not present
    if (!buildGradle.includes('buildFeatures {')) {
      const androidMatch = buildGradle.match(/android\s*\{/);
      if (androidMatch) {
        const insertIndex = androidMatch.index + androidMatch[0].length;
        buildGradle = 
          buildGradle.slice(0, insertIndex) + 
          '\n        buildFeatures {\n            compose = true\n        }' + 
          buildGradle.slice(insertIndex);
      }
    }
    
    // Add composeOptions if not present
    if (!buildGradle.includes('composeOptions {')) {
      const androidMatch = buildGradle.match(/android\s*\{/);
      if (androidMatch) {
        const insertIndex = androidMatch.index + androidMatch[0].length;
        buildGradle = 
          buildGradle.slice(0, insertIndex) + 
          '\n        composeOptions {\n            kotlinCompilerExtensionVersion = "1.5.11"\n        }' + 
          buildGradle.slice(insertIndex);
      }
    }
    
    // Add Compose dependencies if not present
    const composeDependencies = `
    // Compose dependencies for LiquidGlass
    implementation 'androidx.compose.runtime:runtime:1.9.5'
    implementation 'androidx.compose.ui:ui:1.9.5'
    implementation 'androidx.compose.foundation:foundation:1.9.5'
    implementation 'androidx.compose.ui:ui-graphics:1.9.5'
    implementation 'androidx.compose.material3:material3:1.2.1'
    implementation 'androidx.activity:activity-compose:1.9.2'
    implementation 'io.github.kyant0:capsule:2.1.1'`;
    
    if (!buildGradle.includes('androidx.compose.ui:ui:')) {
      const dependenciesMatch = buildGradle.match(/dependencies\s*\{/);
      if (dependenciesMatch) {
        const insertIndex = dependenciesMatch.index + dependenciesMatch[0].length;
        buildGradle = 
          buildGradle.slice(0, insertIndex) + 
          composeDependencies + 
          buildGradle.slice(insertIndex);
      }
    }
    
    fs.writeFileSync(buildGradlePath, buildGradle);
    return config;
  });
  
  // Then, modify settings.gradle
  config = withSettingsGradle(config, (config) => {
    const settingsGradlePath = config.modRequest.platformProjectRoot + '/settings.gradle';
    let settingsGradle = fs.readFileSync(settingsGradlePath, 'utf8');
    
    // Add pluginManagement if not present
    if (!settingsGradle.includes('pluginManagement {')) {
      settingsGradle = `pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    }
}

` + settingsGradle;
    }
    
    // Add backdrop and catalog modules
    const androidLiquidGlassPath = path.resolve(
      config.modRequest.projectRoot,
      '..',
      'AndroidLiquidGlass'
    );
    
    if (fs.existsSync(androidLiquidGlassPath)) {
      const relativePath = path.relative(
        config.modRequest.platformProjectRoot,
        androidLiquidGlassPath
      ).replace(/\\/g, '/');
      
      const backdropInclude = `
include ':backdrop'
project(':backdrop').projectDir = new File('${relativePath}/backdrop')

include ':catalog'
project(':catalog').projectDir = new File('${relativePath}/catalog')`;
      
      if (!settingsGradle.includes("include ':backdrop'")) {
        const lastIncludeMatch = settingsGradle.match(/include\s+['"][^'"]+['"]/g);
        if (lastIncludeMatch) {
          const lastInclude = lastIncludeMatch[lastIncludeMatch.length - 1];
          const insertIndex = settingsGradle.indexOf(lastInclude) + lastInclude.length;
          settingsGradle = 
            settingsGradle.slice(0, insertIndex) + 
            backdropInclude + 
            settingsGradle.slice(insertIndex);
        } else {
          const rootProjectMatch = settingsGradle.match(/rootProject\.name\s*=\s*['"][^'"]+['"]/);
          if (rootProjectMatch) {
            const insertIndex = rootProjectMatch.index + rootProjectMatch[0].length;
            settingsGradle = 
              settingsGradle.slice(0, insertIndex) + 
              backdropInclude + 
              settingsGradle.slice(insertIndex);
          }
        }
      }
    }
    
    fs.writeFileSync(settingsGradlePath, settingsGradle);
    return config;
  });
  
  // Finally, copy native module files
  return withDangerousMod(config, [
    'android',
    async (config) => {
      const projectRoot = config.modRequest.platformProjectRoot;
      const targetJavaPath = path.join(projectRoot, 'app', 'src', 'main', 'java', 'com', 'liquidglassrn');
      
      // Create directory if it doesn't exist
      if (!fs.existsSync(targetJavaPath)) {
        fs.mkdirSync(targetJavaPath, { recursive: true });
      }
      
      // Copy native module files from the library
      const libPath = path.resolve(__dirname, '..', 'android', 'app', 'src', 'main', 'java', 'com', 'liquidglassrn');
      if (fs.existsSync(libPath)) {
        // Copy all Kotlin files
        const files = fs.readdirSync(libPath, { recursive: true });
        files.forEach(file => {
          if (file.endsWith('.kt')) {
            const sourcePath = path.join(libPath, file);
            const targetPath = path.join(targetJavaPath, file);
            const targetDir = path.dirname(targetPath);
            if (!fs.existsSync(targetDir)) {
              fs.mkdirSync(targetDir, { recursive: true });
            }
            fs.copyFileSync(sourcePath, targetPath);
          }
        });
      }
      
      // Find and update MainApplication.kt to include LiquidButtonPackage
      const javaMainPath = path.join(projectRoot, 'app', 'src', 'main', 'java');
      if (fs.existsSync(javaMainPath)) {
        // Find MainApplication.kt recursively
        const findMainApplication = (dir) => {
          const files = fs.readdirSync(dir, { withFileTypes: true });
          for (const file of files) {
            const fullPath = path.join(dir, file.name);
            if (file.isDirectory()) {
              const found = findMainApplication(fullPath);
              if (found) return found;
            } else if (file.name === 'MainApplication.kt') {
              return fullPath;
            }
          }
          return null;
        };
        
        const mainApplicationPath = findMainApplication(javaMainPath);
        if (mainApplicationPath) {
          let mainApplicationContent = fs.readFileSync(mainApplicationPath, 'utf8');
          
          // Add import if not present
          if (!mainApplicationContent.includes('import com.liquidglassrn.LiquidButtonPackage')) {
            const importMatch = mainApplicationContent.match(/(import\s+expo\.modules\.ReactNativeHostWrapper)/);
            if (importMatch) {
              const insertIndex = importMatch.index + importMatch[0].length;
              mainApplicationContent = 
                mainApplicationContent.slice(0, insertIndex) + 
                '\nimport com.liquidglassrn.LiquidButtonPackage' + 
                mainApplicationContent.slice(insertIndex);
            }
          }
          
          // Add package to packages list if not present
          if (!mainApplicationContent.includes('LiquidButtonPackage()')) {
            const packagesMatch = mainApplicationContent.match(/(PackageList\(this\)\.packages\.apply\s*\{)/);
            if (packagesMatch) {
              const insertIndex = packagesMatch.index + packagesMatch[0].length;
              // Find the closing brace of the apply block
              let braceCount = 1;
              let currentIndex = insertIndex;
              while (currentIndex < mainApplicationContent.length && braceCount > 0) {
                if (mainApplicationContent[currentIndex] === '{') braceCount++;
                if (mainApplicationContent[currentIndex] === '}') braceCount--;
                currentIndex++;
              }
              // Insert before the closing brace
              const beforeClosing = mainApplicationContent.lastIndexOf('}', currentIndex - 1);
              if (beforeClosing > insertIndex) {
                mainApplicationContent = 
                  mainApplicationContent.slice(0, beforeClosing) + 
                  '\n              add(LiquidButtonPackage())' + 
                  mainApplicationContent.slice(beforeClosing);
              }
            }
          }
          
          fs.writeFileSync(mainApplicationPath, mainApplicationContent);
        }
      }
      
      return config;
    },
  ]);
};

module.exports = withLiquidGlass;
