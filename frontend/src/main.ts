import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';

import App from './App.vue';
import router from './router';
import { useAuthStore } from './store/modules/auth';
import { setupPermissionDirective } from './directives/permission';
import './styles/index.scss';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);

const authStore = useAuthStore(pinia);
authStore.bootstrapFromStorage();

setupPermissionDirective(app, pinia);

app.use(router);
app.use(ElementPlus, {
  locale: zhCn
});
app.mount('#app');
