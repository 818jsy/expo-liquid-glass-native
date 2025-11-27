const { withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

/**
 * Expo config plugin for expo-liquid-glass-native
 * Automatically configures Compose plugin and dependencies
 */
const withExpoLiquidGlassNative = (config) => {
  return withDangerousMod(config, [
    'android',
    async (config) => {
      const projectRoot = config.modRequest.platformProjectRoot;
      const settingsGradlePath = path.join(projectRoot, 'settings.gradle');
      const appBuildGradlePath = path.join(projectRoot, 'app', 'build.gradle');
      
      // Configure settings.gradle - Add Compose plugin to pluginManagement
      if (fs.existsSync(settingsGradlePath)) {
        let settingsGradle = fs.readFileSync(settingsGradlePath, 'utf8');
        
        // Add Compose plugin to pluginManagement if not present
        if (!settingsGradle.includes('id("org.jetbrains.kotlin.plugin.compose")')) {
          // Check if repositories block exists, if not add it
          if (!settingsGradle.includes('pluginManagement {')) {
            // Add pluginManagement block at the beginning
            const composePluginConfig = `pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  
  plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
  }
  
`;
            settingsGradle = composePluginConfig + settingsGradle;
          } else {
            // pluginManagement exists, add repositories if missing, then add plugins
            if (!settingsGradle.includes('repositories {')) {
              // Add repositories block after pluginManagement
              const pluginManagementMatch = settingsGradle.match(/pluginManagement\s*\{/);
              if (pluginManagementMatch) {
                const insertIndex = pluginManagementMatch.index + pluginManagementMatch[0].length;
                const repositoriesBlock = `
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  
  plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
  }
  `;
                settingsGradle = 
                  settingsGradle.slice(0, insertIndex) +
                  repositoriesBlock +
                  settingsGradle.slice(insertIndex);
              }
            } else if (!settingsGradle.match(/plugins\s*\{[\s\S]*?id\(["']org\.jetbrains\.kotlin\.plugin\.compose["']\)/)) {
              // repositories exists, add plugins block after repositories
              const repositoriesEndMatch = settingsGradle.match(/repositories\s*\{[\s\S]*?\n\}/);
              if (repositoriesEndMatch) {
                const insertIndex = repositoriesEndMatch.index + repositoriesEndMatch[0].length;
                const pluginsBlock = `
  
  plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
  }
  `;
                settingsGradle = 
                  settingsGradle.slice(0, insertIndex) +
                  pluginsBlock +
                  settingsGradle.slice(insertIndex);
              }
            }
          }
        }
        
        fs.writeFileSync(settingsGradlePath, settingsGradle);
      }
      
      // Configure MainApplication.kt - Add LiquidButtonPackage registration
      // Dynamically find MainApplication.kt by searching for it
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
      
      const javaSrcPath = path.join(projectRoot, 'app', 'src', 'main', 'java');
      const mainApplicationPath = fs.existsSync(javaSrcPath) ? findMainApplication(javaSrcPath) : null;
      
      if (mainApplicationPath && fs.existsSync(mainApplicationPath)) {
        let mainApplication = fs.readFileSync(mainApplicationPath, 'utf8');
        
        // Check if LiquidButtonPackage is already added
        if (!mainApplication.includes('expo.modules.liquidglassnative.LiquidButtonPackage')) {
          // Find the getPackages function and add the package
          const packagesMatch = mainApplication.match(/getPackages\(\):\s*List<ReactPackage>\s*=\s*PackageList\(this\)\.packages\.apply\s*\{/);
          if (packagesMatch) {
            const insertIndex = packagesMatch.index + packagesMatch[0].length;
            // Find the closing brace of the apply block
            let braceCount = 1;
            let currentIndex = insertIndex;
            let closingBraceIndex = -1;
            
            while (currentIndex < mainApplication.length && braceCount > 0) {
              if (mainApplication[currentIndex] === '{') braceCount++;
              if (mainApplication[currentIndex] === '}') {
                braceCount--;
                if (braceCount === 0) {
                  closingBraceIndex = currentIndex;
                  break;
                }
              }
              currentIndex++;
            }
            
            if (closingBraceIndex !== -1) {
              // Check if there's already a comment or add statement
              const beforeClosing = mainApplication.slice(insertIndex, closingBraceIndex);
              const packageAddStatement = `\n              add(expo.modules.liquidglassnative.LiquidButtonPackage())\n`;
              
              // Find the last add statement or comment before closing brace
              const lastAddMatch = beforeClosing.match(/(add\([^)]+\)|\/\/[^\n]*)/g);
              if (lastAddMatch) {
                const lastAdd = lastAddMatch[lastAddMatch.length - 1];
                const lastAddIndex = beforeClosing.lastIndexOf(lastAdd);
                const insertPos = insertIndex + lastAddIndex + lastAdd.length;
                mainApplication = 
                  mainApplication.slice(0, insertPos) +
                  packageAddStatement +
                  mainApplication.slice(insertPos);
              } else {
                // Insert after the opening brace
                mainApplication = 
                  mainApplication.slice(0, insertIndex) +
                  packageAddStatement +
                  mainApplication.slice(insertIndex);
              }
              
              fs.writeFileSync(mainApplicationPath, mainApplication);
            }
          }
        }
      }
      
      // Configure app/build.gradle - Add Compose plugin and dependencies
      if (fs.existsSync(appBuildGradlePath)) {
        let appBuildGradle = fs.readFileSync(appBuildGradlePath, 'utf8');
        
        // Convert apply plugin to plugins block if needed, or add Compose plugin
        if (appBuildGradle.includes('apply plugin:')) {
          // Convert apply plugin to plugins block
          if (!appBuildGradle.includes('plugins {')) {
            const firstApplyPlugin = appBuildGradle.match(/apply plugin:/);
            if (firstApplyPlugin) {
              const plugins = [];
              let match;
              const applyPluginRegex = /apply plugin:\s*["']([^"']+)["']/g;
              while ((match = applyPluginRegex.exec(appBuildGradle)) !== null) {
                plugins.push(match[1]);
              }
              
              if (plugins.length > 0) {
                const pluginsBlock = `plugins {\n${plugins.map(p => `    id("${p}")`).join('\n')}\n    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"\n}\n\n`;
                appBuildGradle = appBuildGradle.replace(/apply plugin:\s*["'][^"']+["']\n/g, '');
                appBuildGradle = pluginsBlock + appBuildGradle;
              }
            }
          } else {
            // plugins block exists, just add Compose plugin
            if (!appBuildGradle.includes('id("org.jetbrains.kotlin.plugin.compose")')) {
              const pluginsMatch = appBuildGradle.match(/plugins\s*\{/);
              if (pluginsMatch) {
                const insertIndex = pluginsMatch.index + pluginsMatch[0].length;
                const composePlugin = `
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
`;
                appBuildGradle = 
                  appBuildGradle.slice(0, insertIndex) +
                  composePlugin +
                  appBuildGradle.slice(insertIndex);
              }
            }
          }
        } else if (appBuildGradle.includes('plugins {')) {
          // plugins block exists, just add Compose plugin
          if (!appBuildGradle.includes('id("org.jetbrains.kotlin.plugin.compose")')) {
            const pluginsMatch = appBuildGradle.match(/plugins\s*\{/);
            if (pluginsMatch) {
              const insertIndex = pluginsMatch.index + pluginsMatch[0].length;
              const composePlugin = `
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
`;
              appBuildGradle = 
                appBuildGradle.slice(0, insertIndex) +
                composePlugin +
                appBuildGradle.slice(insertIndex);
            }
          }
        }
        
        // Add Compose dependencies if not present
        if (!appBuildGradle.includes('androidx.compose.runtime:runtime:')) {
          const dependenciesMatch = appBuildGradle.match(/dependencies\s*\{/);
          if (dependenciesMatch) {
            const insertIndex = dependenciesMatch.index + dependenciesMatch[0].length;
            const composeDependencies = `
    // Compose dependencies (required for expo-liquid-glass-native)
    implementation("androidx.compose.runtime:runtime:1.9.5")
    implementation("androidx.compose.foundation:foundation:1.9.5")
    implementation("androidx.compose.ui:ui:1.9.5")
    implementation("androidx.compose.ui:ui-graphics:1.9.5")
`;
            appBuildGradle = 
              appBuildGradle.slice(0, insertIndex) +
              composeDependencies +
              appBuildGradle.slice(insertIndex);
          }
        }
        
        // Add kotlin block for jvmToolchain if not present
        if (!appBuildGradle.includes('kotlin {')) {
          const androidBlockMatch = appBuildGradle.match(/(android\s*\{[\s\S]*?\n\})/);
          if (androidBlockMatch) {
            const insertIndex = androidBlockMatch.index + androidBlockMatch[0].length;
            const kotlinBlock = `

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xskip-metadata-version-check"
        )
    }
}
`;
            appBuildGradle = 
              appBuildGradle.slice(0, insertIndex) +
              kotlinBlock +
              appBuildGradle.slice(insertIndex);
          }
        }
        
        fs.writeFileSync(appBuildGradlePath, appBuildGradle);
      }
      
      return config;
    },
  ]);
};

module.exports = withExpoLiquidGlassNative;

