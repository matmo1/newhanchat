# Stage 1: Build with Vite
FROM node:24-slim AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build  # Outputs to /dist

# Stage 2: Serve with Vite Preview
FROM node:24-alpine
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package.json .
COPY --from=builder /app/node_modules ./node_modules

# Install vite globally to use 'vite preview'
RUN npm install -g vite

EXPOSE 4173 
CMD ["vite", "preview", "--host", "0.0.0.0", "--port", "4173"]