import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import playwright from 'eslint-plugin-playwright';
import prettier from 'eslint-config-prettier';

export default tseslint.config(
  eslint.configs.recommended,
  tseslint.configs.recommended,
  {
    files: ['test/**/*.ts'],
    ...playwright.configs['flat/recommended'],
  },
  prettier,
  {
    ignores: ['node_modules', 'dist', 'test-results', 'playwright-report'],
  },
);
