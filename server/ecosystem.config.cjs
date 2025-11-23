module.exports = {
  apps: [
    {
      name: "futureme-api",
      script: "src/index.js",
      cwd: __dirname,
      interpreter: "node",
      watch: false,
      env: {
        NODE_ENV: "production",
        PORT: process.env.PORT || 8080
      }
    }
  ]
};
