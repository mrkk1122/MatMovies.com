/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        darkBg: '#0b0c10',
        darkSurface: '#1f2833',
        primaryGreen: 'rgb(var(--primary-green-rgb, 0 255 102))',
        primaryDark: '#0b0c10',
        accentSlate: '#c5c6c7',
        accentCyan: '#66fcf1',
        neonPurple: '#8a2be2'
      },
      boxShadow: {
        'accent-glow': '0 2px 8px rgba(var(--primary-green-rgb, 0 255 102), 0.2)',
        'accent-glow-lg': '0 4px 12px rgba(var(--primary-green-rgb, 0 255 102), 0.15)',
        'accent-glow-xl': '0 0 10px rgba(var(--primary-green-rgb, 0 255 102), 0.15)',
        'accent-glow-button': '0 4px 14px rgba(var(--primary-green-rgb, 0 255 102), 0.3)',
        'accent-glow-button-hover': '0 6px 20px rgba(var(--primary-green-rgb, 0 255 102), 0.4)',
      }
    },
  },
  plugins: [],
}
