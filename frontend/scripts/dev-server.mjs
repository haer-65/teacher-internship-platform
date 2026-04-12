import { createServer } from 'vite';
import { createFrontendViteConfig } from './vite.shared.mjs';
const mode = process.env.NODE_ENV || 'development';
const viteConfig = createFrontendViteConfig(mode);

const server = await createServer({
  ...viteConfig,
  configFile: false
});

await server.listen();
server.printUrls();
