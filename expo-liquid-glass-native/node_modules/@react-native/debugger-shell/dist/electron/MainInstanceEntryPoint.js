"use strict";

const { BrowserWindow, app, shell, ipcMain } = require("electron");
const path = require("path");
const util = require("util");
const windowMetadata = new WeakMap();
function handleLaunchArgs(argv) {
  const {
    values: { frontendUrl, windowKey },
  } = util.parseArgs({
    options: {
      frontendUrl: {
        type: "string",
      },
      windowKey: {
        type: "string",
      },
    },
    args: argv,
  });
  let frontendWindow = BrowserWindow.getAllWindows().find((window) => {
    const metadata = windowMetadata.get(window);
    if (!metadata) {
      return false;
    }
    return metadata.windowKey === windowKey;
  });
  if (frontendWindow) {
    if (frontendWindow.isVisible()) {
      frontendWindow.flashFrame(true);
      setTimeout(() => {
        frontendWindow.flashFrame(false);
      }, 1000);
    }
  } else {
    frontendWindow = new BrowserWindow({
      width: 1200,
      height: 600,
      webPreferences: {
        partition: "persist:react-native-devtools",
        preload: require.resolve("./preload.js"),
      },
      icon: path.join(__dirname, "resources", "icon.png"),
    });
    frontendWindow.setMenuBarVisibility(false);
  }
  frontendWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return {
      action: "deny",
    };
  });
  frontendWindow.loadURL(frontendUrl);
  windowMetadata.set(frontendWindow, {
    windowKey,
  });
  if (process.platform === "darwin") {
    app.focus({
      steal: true,
    });
  }
  frontendWindow.focus();
}
app.whenReady().then(() => {
  handleLaunchArgs(process.argv.slice(app.isPackaged ? 1 : 2));
  app.on(
    "second-instance",
    (event, electronArgv, workingDirectory, additionalData) => {
      handleLaunchArgs(additionalData.argv);
    },
  );
});
app.on("window-all-closed", function () {
  app.quit();
});
ipcMain.on("bringToFront", (event, title) => {
  const webContents = event.sender;
  const win = BrowserWindow.fromWebContents(webContents);
  if (win) {
    win.focus();
  }
  if (process.platform === "darwin") {
    app.focus({
      steal: true,
    });
  }
});
