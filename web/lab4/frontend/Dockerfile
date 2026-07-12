# Stage 1: Build the React app using Node.js 20
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package.json package-lock.json ./

# Install dependencies, including patch-package
RUN npm install -g patch-package && npm ci --omit=dev

# Copy source code
COPY . .

# Apply patches (if any)
RUN patch-package || true

# Build the React app
RUN npm run build

# Stage 2: Serve the React app using Nginx
FROM nginx:1.21-alpine

# Remove default Nginx configuration
RUN rm -rf /etc/nginx/conf.d

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy the built React app from the build stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]