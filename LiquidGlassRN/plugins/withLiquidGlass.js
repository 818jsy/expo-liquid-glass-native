const { withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

/**
 * Expo config plugin to integrate AndroidLiquidGlass native module
 */
const withLiquidGlass = (config) => {
  return withDangerousMod(config, [
    'android',
    async (config) => {
      const projectRoot = config.modRequest.platformProjectRoot;
      const buildGradlePath = path.join(projectRoot, 'app', 'build.gradle');
      
      // Read build.gradle
      let buildGradle = fs.readFileSync(buildGradlePath, 'utf8');
      
      // Add backdrop library dependency
      // We'll use the local module from AndroidLiquidGlass
      const backdropDependency = `
    // LiquidGlass backdrop library
    implementation project(':backdrop')`;
      
      // Find dependencies block and add our dependency
      if (!buildGradle.includes('implementation project(\':backdrop\')')) {
        const dependenciesMatch = buildGradle.match(/dependencies\s*\{/);
        if (dependenciesMatch) {
          const insertIndex = dependenciesMatch.index + dependenciesMatch[0].length;
          buildGradle = 
            buildGradle.slice(0, insertIndex) + 
            backdropDependency + 
            buildGradle.slice(insertIndex);
        }
      }
      
      // Add Compose dependencies if not present
      const composeDependencies = `
    // Compose dependencies for LiquidGlass (matching original project)
    implementation 'androidx.compose.runtime:runtime:1.9.5'
    implementation 'androidx.compose.ui:ui:1.9.5'
    implementation 'androidx.compose.foundation:foundation:1.9.5'
    implementation 'androidx.compose.ui:ui-graphics:1.9.5'
    implementation 'androidx.compose.material3:material3:1.2.1'
    implementation 'androidx.activity:activity-compose:1.9.2'`;
      
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
      
      // Write back to file
      fs.writeFileSync(buildGradlePath, buildGradle);
      
      // Update settings.gradle to include backdrop module
      const settingsGradlePath = path.join(projectRoot, 'settings.gradle');
      if (fs.existsSync(settingsGradlePath)) {
        let settingsGradle = fs.readFileSync(settingsGradlePath, 'utf8');
        
        // Add backdrop module if not already included
        if (!settingsGradle.includes("include ':backdrop'")) {
          const androidLiquidGlassPath = path.relative(
            projectRoot,
            path.join(config.modRequest.projectRoot, '..', 'AndroidLiquidGlass')
          ).replace(/\\/g, '/');
          
          const backdropInclude = `
include ':backdrop'
project(':backdrop').projectDir = new File('${androidLiquidGlassPath}/backdrop')`;
          
          // Find the end of include statements
          const lastIncludeMatch = settingsGradle.match(/include\s+['"][^'"]+['"]/g);
          if (lastIncludeMatch) {
            const lastInclude = lastIncludeMatch[lastIncludeMatch.length - 1];
            const insertIndex = settingsGradle.indexOf(lastInclude) + lastInclude.length;
            settingsGradle = 
              settingsGradle.slice(0, insertIndex) + 
              backdropInclude + 
              settingsGradle.slice(insertIndex);
          } else {
            // If no includes found, add after rootProject
            const rootProjectMatch = settingsGradle.match(/rootProject\.name\s*=\s*['"][^'"]+['"]/);
            if (rootProjectMatch) {
              const insertIndex = rootProjectMatch.index + rootProjectMatch[0].length;
              settingsGradle = 
                settingsGradle.slice(0, insertIndex) + 
                backdropInclude + 
                settingsGradle.slice(insertIndex);
            }
          }
          
          fs.writeFileSync(settingsGradlePath, settingsGradle);
        }
      }
      
      return config;
    },
  ]);
};

module.exports = withLiquidGlass;

