/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @generated SignedSource<<0775a48842b446a010cffb7242ce1d39>>
 *
 * This file was translated from Flow by scripts/js-api/build-types/index.js.
 * Original file: packages/react-native/src/private/components/virtualview/VirtualView.js
 */

import type { ViewStyleProp } from "../../../../Libraries/StyleSheet/StyleSheet";
import type { HostInstance } from "../../types/HostInstance";
import type { NativeModeChangeEvent } from "./VirtualViewNativeComponent";
import VirtualViewNativeComponent from "./VirtualViewNativeComponent";
import * as React from "react";
export declare enum VirtualViewMode {
  Visible = 0,
  Prerender = 1,
  Hidden = 2,
}
export declare namespace VirtualViewMode {
  export function cast(value: number | null | undefined): VirtualViewMode;
  export function isValid(value: number | null | undefined): value is VirtualViewMode;
  export function members(): IterableIterator<VirtualViewMode>;
  export function getName(value: VirtualViewMode): string;
}
export declare enum VirtualViewRenderState {
  Unknown = 0,
  Rendered = 1,
  None = 2,
}
export declare namespace VirtualViewRenderState {
  export function cast(value: number | null | undefined): VirtualViewRenderState;
  export function isValid(value: number | null | undefined): value is VirtualViewRenderState;
  export function members(): IterableIterator<VirtualViewRenderState>;
  export function getName(value: VirtualViewRenderState): string;
}
export type Rect = Readonly<{
  x: number;
  y: number;
  width: number;
  height: number;
}>;
export type ModeChangeEvent = Readonly<Omit<Omit<NativeModeChangeEvent, "mode">, keyof {
  mode: VirtualViewMode;
  target: HostInstance;
}> & {
  mode: VirtualViewMode;
  target: HostInstance;
}>;
type VirtualViewComponent = (props: {
  children?: React.ReactNode;
  nativeID?: string;
  ref?: null | undefined | React.Ref<React.ComponentRef<typeof VirtualViewNativeComponent>>;
  style?: null | undefined | ViewStyleProp;
  onModeChange?: (event: ModeChangeEvent) => void;
  removeClippedSubviews?: boolean;
}) => React.ReactNode;
type HiddenHeight = number;
declare const NotHidden: null;
type State = HiddenHeight | typeof NotHidden;
declare const $$VirtualView: VirtualViewComponent;
declare type $$VirtualView = typeof $$VirtualView;
export default $$VirtualView;
export declare const VirtualViewExperimental: VirtualViewComponent;
export declare type VirtualViewExperimental = typeof VirtualViewExperimental;
export declare function createHiddenVirtualView(height: number, experimental: boolean): VirtualViewComponent;
export declare const _logs: {
  states?: Array<State>;
};
export declare type _logs = typeof _logs;
