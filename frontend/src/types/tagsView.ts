import type { RouteLocationNormalizedLoaded } from "vue-router";

export interface TagView extends Partial<RouteLocationNormalizedLoaded> {
  title?: string;
}
