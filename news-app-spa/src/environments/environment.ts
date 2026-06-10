export const environment = {
  production: false,
  // Server root, can be '' for same-origin or '/api' for proxy setups
  // For development, ng serve --proxy-config proxy.conf.json
  // Elastic search without ssl command - elasticsearch.bat -E xpack.security.enabled=false
  // NOTE: Feature-specific endpoints are defined in respective feature's constants/
  // Example: src/app/features/news/constants/news-api.constant.ts
  //Run news-create-page audit using MASTER_CONSISTENCY_ARCHITECTURE.md
  //currently working - FORM_FIELDS_COMPONENT_LIBRARY_PLAN.md (12 KB)
  apiBaseUrl: 'http://localhost:8080'
};
