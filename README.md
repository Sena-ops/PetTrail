# PetTrail — Backend & PWA (Leaflet + OpenStreetMap)

Aplicação com backend em **Java Spring Boot** e frontend **PWA** que registra caminhadas de pets e renderiza rotas em tiles do **OpenStreetMap** via **Leaflet**.

## Sumário
- [Arquitetura / Estrutura](#arquitetura--estrutura)
- [Pré-requisitos](#pré-requisitos)
- [Backend — Como rodar e documentação](#backend--como-rodar-e-documentação)
- [Frontend (PWA) — Como rodar](#frontend-pwa--como-rodar)
- [Ambiente / Configuração](#ambiente--configuração)
- [Nota sobre uso do OpenStreetMap](#nota-sobre-uso-do-openstreetmap)
- [Solução de problemas (Troubleshooting)](#solução-de-problemas-troubleshooting)
- [Teste rápido da API](#teste-rápido-da-api)

---

## Arquitetura / Estrutura

```
backend/
├── src/main/java/...          # Código Spring Boot (controllers, services, repos)
├── src/main/resources/
│   ├── application.properties # Config do servidor (porta, CORS, etc.)
│   └── web/                   # Arquivos da PWA (index.html, app.js, styles, manifest, sw.js)
└── README.md
```

> A PWA fica em `backend/src/main/resources/web/` e pode ser servida pelo próprio Spring Boot ou por um servidor estático local.

---

## Pré-requisitos

- **Java 21+**
- **Maven** _ou_ **Gradle**
- **Node.js 18+** (apenas se optar por um servidor estático local, como `http-server`)

---

## Backend — Como rodar e documentação

### Rodar (Maven)
```bash
# a partir da raiz do projeto
cd backend
mvn spring-boot:run
```

### Rodar (Gradle)
```bash
# a partir da raiz do projeto
cd backend
./gradlew bootRun   # no Windows: gradlew.bat bootRun
```

**Porta padrão:** 8080 (configurável).

**Swagger UI** (se habilitado): http://localhost:8080/swagger-ui/index.html

**OpenAPI JSON:** http://localhost:8080/v3/api-docs

**Endpoints principais:**
- `POST /api/pets`, `GET /api/pets`
- `POST /api/walks/start?petId=...`
- `POST /api/walks/{id}/points`
- `POST /api/walks/{id}/stop`
- `GET /api/walks?petId=...&page=...&size=...`
- `GET /api/walks/{id}/geojson`

---

## Frontend (PWA) — Como rodar

Há duas formas de servir a PWA:

### A) Servir pelo Spring Boot (mais simples)

Coloque os arquivos da PWA em `backend/src/main/resources/web/` (já está assim).
O Spring Boot servirá o `/index.html` em `http://localhost:8080/` (ou na porta configurada).

### B) Servir com servidor estático local (para desenvolvimento do frontend)
```bash
# a partir da raiz do projeto
cd backend/src/main/resources
npx http-server web -p 5173 --cors
# PWA disponível em: http://localhost:5173
```

> Se usar o modo B, garanta que o backend (:8080) permita CORS para `http://localhost:5173`.

---

## Ambiente / Configuração

Edite `backend/src/main/resources/application.properties` conforme necessário:

```properties
# Porta do servidor
server.port=8080

# CORS (permitir o servidor estático local durante o dev)
spring.web.cors.allowed-origins=http://localhost:5173
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true

# Local dos recursos estáticos (servindo a PWA)
# Opcional: apontar para classpath:/web/
spring.web.resources.static-locations=classpath:/web/
```

> Se a PWA for hospedada em outro caminho, ajuste as rotas ou adicione um controller simples para encaminhar `/` para `index.html`.

---

## Nota sobre uso do OpenStreetMap

A PWA usa Leaflet com a API de tiles do OpenStreetMap:

```
https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png
```

- Mantenha a **atribuição obrigatória** do OSM visível.
- **Não faça pré-cache** dos tiles OSM para uso offline. O app shell pode ser cacheado, mas os tiles podem não estar disponíveis offline.

---

## Solução de problemas (Troubleshooting)

### Vejo a página errada (sem `<select>`).
Garanta que o mapeamento estático aponta para `classpath:/web/` e que o `index.html` esperado está dentro de `src/main/resources/web/`.

### Erros de CORS ao rodar a PWA na porta 5173.
Inclua `http://localhost:5173` em `spring.web.cors.allowed-origins`.

### Swagger não carrega.
Confira as dependências do Springdoc e acesse: http://localhost:8080/swagger-ui/index.html.

### Tiles do OSM não aparecem.
Verifique o console do navegador para erros de conteúdo misto/CORS. Use `https://` e mantenha a URL padrão dos tiles OSM.

---

## Teste rápido da API

### Criar um pet:
```bash
curl -s -X POST "http://localhost:8080/api/pets" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Rex", "species": "CACHORRO" }'
```

### Iniciar uma caminhada:
```bash
curl -s -X POST "http://localhost:8080/api/walks/start?petId=1"
```

### Enviar pontos (lote):
```bash
curl -s -X POST "http://localhost:8080/api/walks/123/points" \
  -H "Content-Type: application/json" \
  -d '[{"lat":-23.5505,"lon":-46.6333,"ts":"2025-08-14T22:00:00Z"}]'
```

### Encerrar a caminhada:
```bash
curl -s -X POST "http://localhost:8080/api/walks/123/stop"
```

### Obter rota (GeoJSON):
```bash
curl -s "http://localhost:8080/api/walks/123/geojson"
```
