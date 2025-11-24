/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @generated SignedSource<<c6a67548cfcf329ff3a26ab2a8723aea>>
 *
 * This file was translated from Flow by scripts/js-api/build-types/index.js.
 * Original file: packages/react-native/Libraries/ReactNative/ReactFabricPublicInstance/ReactFabricHostComponent.js
 */

import type { HostInstance, MeasureInWindowOnSuccessCallback, MeasureLayoutOnSuccessCallback, MeasureOnSuccessCallback, NativeMethods } from "../../../src/private/types/HostInstance";
import type { InternalInstanceHandle, ViewConfig } from "../../Renderer/shims/ReactNativeTypes";
/**
 * This is used for refs on host components.
 */
declare class ReactFabricHostComponent implements NativeMethods {
  constructor(tag: number, viewConfig: ViewConfig, internalInstanceHandle: InternalInstanceHandle);
  blur(): void;
  focus(): void;
  measure(callback: MeasureOnSuccessCallback): void;
  measureInWindow(callback: MeasureInWindowOnSuccessCallback): void;
  measureLayout(relativeToNativeNode: number | HostInstance, onSuccess: MeasureLayoutOnSuccessCallback, onFail?: () => void): void;
  unstable_getBoundingClientRect(): DOMRect;
  setNativeProps(nativeProps: {}): void;
}
export default ReactFabricHostComponent;
