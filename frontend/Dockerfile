# --- Stage 1: Build the React App ---
FROM node:18-alpine as build
WORKDIR /app

# Install dependencies (cached if package.json doesn't change)
COPY package*.json ./
RUN npm install

# Copy source code
COPY . .

# Set the API URL for the build (Browser connects to localhost:8080)
ENV VITE_API_BASE_URL=http://localhost:8080/api

# Build the production bundle
RUN npm run build

# --- Stage 2: Serve with Nginx ---
FROM nginx:alpine

# Copy the build output from Stage 1 to Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copy our custom Nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]