import { build } from 'vite';
import { createFrontendViteConfig } from './vite.shared.mjs';

const mode = process.env.NODE_ENV || 'production';

await build({
  ...createFrontendViteConfig(mode),
  configFile: false
});
