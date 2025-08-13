# OpenAPI/Swagger Documentation - PatTrail

## ✅ Critérios de Aceite Implementados

### 1. Endpoints Acessíveis
- ✅ `/v3/api-docs` - Documentação OpenAPI em formato JSON
- ✅ `/swagger-ui.html` - Interface Swagger UI para testar os endpoints

### 2. Endpoints Criados com Schemas Corretos
- ✅ Todos os endpoints criados nas sprints exibidos com schemas corretos
- ✅ Documentação completa com exemplos, descrições e códigos de resposta

## 🚀 Como Acessar

### 1. Iniciar a Aplicação
```bash
cd backend
./mvnw spring-boot:run
```

### 2. Acessar a Documentação
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 📋 Endpoints Disponíveis

### Pets API (`/api/pets`)
- `GET /api/pets` - Listar todos os pets
- `GET /api/pets/{id}` - Buscar pet por ID
- `POST /api/pets` - Criar novo pet
- `PUT /api/pets/{id}` - Atualizar pet
- `DELETE /api/pets/{id}` - Excluir pet
- `GET /api/pets/especie/{especie}` - Buscar pets por espécie
- `GET /api/pets/busca?nome={nome}` - Buscar pets por nome

### Health Check API (`/api/health`)
- `GET /api/health` - Verificar status da aplicação
- `GET /api/health/ping` - Ping simples

## 🔧 Configuração OpenAPI

### Dependências Adicionadas
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

### Configuração no application.properties
```properties
# OpenAPI/Swagger Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.doc-expansion=none
springdoc.swagger-ui.disable-swagger-default-url=true
```

### Classe de Configuração
- `OpenApiConfig.java` - Configuração personalizada do OpenAPI

## 📝 Anotações Utilizadas

### Controllers
- `@Tag` - Agrupa endpoints por categoria
- `@Operation` - Descreve a operação
- `@ApiResponses` - Define códigos de resposta
- `@Parameter` - Documenta parâmetros

### Models
- `@Schema` - Documenta propriedades do modelo
- Validações com `@NotBlank`, `@NotNull`, `@Positive`

### DTOs
- `@Schema` - Documenta campos de entrada
- Validações Bean Validation

## 🧪 Testando os Endpoints

### 1. Via Swagger UI
1. Acesse http://localhost:8080/swagger-ui.html
2. Clique em qualquer endpoint
3. Clique em "Try it out"
4. Preencha os parâmetros
5. Clique em "Execute"

### 2. Via curl
```bash
# Listar pets
curl -X GET "http://localhost:8080/api/pets"

# Criar pet
curl -X POST "http://localhost:8080/api/pets" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Rex",
    "especie": "Cachorro",
    "raca": "Golden Retriever",
    "idade": 3
  }'

# Health check
curl -X GET "http://localhost:8080/api/health"
```

## 📊 Schemas Gerados Automaticamente

O OpenAPI gera automaticamente schemas para:
- ✅ Entidade `Pet` com todas as propriedades
- ✅ DTO `CriarPetRequest` para criação de pets
- ✅ Respostas de erro padronizadas
- ✅ Códigos de status HTTP apropriados

## 🎯 Benefícios Implementados

1. **Documentação Automática**: Todos os endpoints são documentados automaticamente
2. **Interface Interativa**: Swagger UI permite testar endpoints diretamente
3. **Schemas Corretos**: Modelos de dados bem documentados com exemplos
4. **Validações**: Bean Validation integrado com documentação
5. **Códigos de Resposta**: Documentação completa de todos os códigos HTTP
6. **Exemplos**: Exemplos práticos para cada endpoint
