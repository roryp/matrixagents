export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        matrix: {
          black: '#0d0d0d',
          dark: '#1a1a2e',
          primary: '#00ff41',
          secondary: '#008f11',
          accent: '#003b00',
          glow: '#39ff14',
        },
      },
      animation: {
        'pulse-glow': 'pulse-glow 2s ease-in-out infinite',
        'flow': 'flow 1.5s ease-in-out infinite',
        'scan': 'scan 2s linear infinite',
        'fade-in': 'fade-in 0.5s ease-out',
        'slide-up': 'slide-up 0.3s ease-out',
      },
      keyframes: {
        'pulse-glow': {
          '0%, 100%': { boxShadow: '0 0 5px #00ff41, 0 0 10px #00ff41, 0 0 15px #00ff41' },
          '50%': { boxShadow: '0 0 10px #00ff41, 0 0 20px #00ff41, 0 0 30px #00ff41' },
        },
        'flow': {
          '0%': { strokeDashoffset: '100' },
          '100%': { strokeDashoffset: '0' },
        },
        'scan': {
          '0%': { backgroundPosition: '0% 0%' },
          '100%': { backgroundPosition: '0% 100%' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'slide-up': {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
}
