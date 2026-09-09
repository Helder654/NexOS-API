# NexOS API

API REST para o gerenciamento de uma assistência técnica de consoles. O projeto nasceu da evolução de um sistema acadêmico e está sendo reconstruído de forma incremental para consolidar boas práticas de desenvolvimento backend com Java e Spring Boot.

Nesta primeira versão, a API permite cadastrar clientes e controlar ordens de serviço, desde a abertura até o acompanhamento do reparo.

## Objetivo

O NexOS centraliza informações importantes para uma assistência técnica:

- clientes e seus dados de contato;
- console recebido e defeito relatado;
- análise técnica e diagnóstico;
- valor cobrado e custo interno do reparo;
- andamento da ordem de serviço.

O backend foi pensado para ser consumido futuramente por uma aplicação web, mobile ou outro cliente HTTP.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Lombok
- Bean Validation
- H2 para testes de integração
- Flyway para migrações de banco de dados
- Springdoc OpenAPI / Swagger UI

## Arquitetura

O código está organizado por responsabilidade:

```text
com.example.nexos
├── controllers   # Endpoints HTTP e códigos de resposta
├── services      # Regras de negócio
├── repositories  # Acesso aos dados com Spring Data JPA
├── models        # Entidades e enum de status
├── dtos          # Contratos de entrada e saída da API
├── mappers       # Conversão entre DTOs e entidades
├── exceptions    # Exceções de negócio e respostas de erro
└── config        # Configurações, como OpenAPI
```

Essa separação evita expor entidades JPA diretamente na API e mantém as regras de negócio fora dos controllers.

## Funcionalidades atuais

### Clientes

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/clients` | Cadastra um cliente |
| `GET` | `/clients` | Lista clientes de forma paginada |
| `GET` | `/clients/{id}` | Busca um cliente por ID |
| `PUT` | `/clients/{id}` | Atualiza os dados de um cliente |
| `DELETE` | `/clients/{id}` | Remove um cliente |

### Ordens de serviço

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/service-orders` | Abre uma ordem de serviço |
| `GET` | `/service-orders` | Lista ordens de serviço de forma paginada |
| `GET` | `/service-orders/{id}` | Busca uma ordem por ID |
| `PUT` | `/service-orders/{id}` | Atualiza dados técnicos e financeiros da ordem |
| `PATCH` | `/service-orders/{id}/status` | Atualiza somente o status da ordem |
| `DELETE` | `/service-orders/{id}` | Exclui uma ordem em situação permitida |

Ao abrir uma ordem, o status inicial é `ABERTA` e a data de abertura é definida pelo servidor.

O `PUT` não altera cliente, data de abertura nem status. Essas informações têm endpoints e regras próprias, evitando atualizações acidentais.

Uma ordem pode ser excluída somente enquanto estiver `ABERTA` ou depois de `CANCELADA`. Ordens em análise, reparo ou já finalizadas preservam seu histórico e retornam `409 Conflict` caso a exclusão seja solicitada.

## Paginação e ordenação

As listagens de clientes e ordens de serviço aceitam os parâmetros `page`, `size` e `sort`. A resposta inclui os registros em `content` e metadados como página atual, total de elementos e total de páginas.

```text
GET /clients?page=0&size=10&sort=nome,asc
GET /service-orders?page=0&size=10&sort=dataAbertura,desc
```

Sem parâmetros, a API retorna a primeira página com até 10 registros. Clientes são ordenados por `id` e ordens por data de abertura decrescente.

## Filtros de ordens de serviço

O endpoint de listagem de ordens pode combinar filtros com paginação e ordenação:

```text
GET /service-orders?clienteId=1&status=EM_ANALISE&dataAberturaInicial=2026-01-01&dataAberturaFinal=2026-01-31&page=0&size=10
```

Filtros disponíveis: `clienteId`, `status`, `dataAberturaInicial` e `dataAberturaFinal`. Datas devem usar o formato `yyyy-MM-dd`; o período é inclusivo e a data inicial não pode ser posterior à final.

## Fluxo de status da ordem

```text
ABERTA → EM_ANALISE → AGUARDANDO_APROVACAO → EM_REPARO → FINALIZADA
   └──────────────→ CANCELADA
```

O cancelamento é permitido antes da finalização. Estados `FINALIZADA` e `CANCELADA` são finais. Uma transição inválida retorna `409 Conflict` com uma mensagem explicativa.

## Validações e respostas de erro

Os DTOs validam campos obrigatórios, tamanho de textos e valores monetários não negativos antes que a regra de negócio seja executada.

| Situação | Resposta |
| --- | --- |
| Corpo de requisição inválido | `400 Bad Request` |
| Cliente ou ordem não encontrada | `404 Not Found` |
| Transição de status inválida | `409 Conflict` |

Os erros usam o formato `ProblemDetail` do Spring, deixando a resposta consistente para quem consumir a API.

## Exemplo de abertura de ordem

```http
POST /service-orders
Content-Type: application/json
```

```json
{
  "clienteId": 1,
  "console": "PlayStation 5",
  "defeitoRelatado": "Console não liga",
  "analiseTecnico": "Fonte em análise",
  "diagnostico": "Possível falha na fonte",
  "valor": 350.00,
  "custoReparo": 180.00
}
```

## Como executar localmente

### Pré-requisitos

- JDK 21 ou superior compatível
- PostgreSQL em execução

Configure as variáveis de ambiente usadas pela aplicação:

```text
DATA_BASE_URL=jdbc:postgresql://localhost:5432/nexos
DATA_BASE_USERNAME=postgres
DATA_BASE_PASSWORD=sua_senha
```

Depois, inicie a API usando o Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação será iniciada, por padrão, em `http://localhost:8080`.

## Migrações de banco de dados

O schema é versionado com Flyway. A migração `V1__create_initial_schema.sql` cria as tabelas de clientes e ordens de serviço, além dos índices usados nas consultas por cliente, status e data de abertura.

O Hibernate utiliza `ddl-auto=validate`: ele confere se as entidades correspondem ao schema, mas não cria nem altera tabelas. Toda evolução estrutural deve ser adicionada como uma nova migração em `src/main/resources/db/migration`.

Para compatibilidade com um banco local já criado antes da adoção do Flyway, a aplicação usa `baseline-on-migrate=true`. Em ambientes com dados importantes, faça backup e revise a migração antes da primeira execução.

## Documentação interativa

Com a aplicação em execução, a documentação pode ser acessada em:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI em JSON: `http://localhost:8080/v3/api-docs`

O Swagger UI permite visualizar contratos, campos, respostas e executar requisições diretamente pelo navegador.

## Testes

O projeto possui testes de integração para os endpoints de clientes, ordens de serviço e documentação OpenAPI. Os testes usam H2 em modo de compatibilidade com PostgreSQL, sem depender do banco local.

```powershell
.\mvnw.cmd clean test
```

Atualmente, a suíte possui 36 testes automatizados cobrindo cenários de sucesso, validação, recursos inexistentes, regras de exclusão, paginação, ordenação, filtros, migração de banco e transições de status inválidas.

## Etapas já desenvolvidas

1. **Fundação do projeto** — Estrutura Spring Boot, entidade de cliente, JPA, DTOs, mappers e configuração do PostgreSQL.
2. **CRUD de clientes** — Criação, consulta individual, listagem, atualização e remoção, com validações e tratamento de `404`.
3. **Abertura de ordens de serviço** — Associação obrigatória com cliente, informações do console e valores de reparo, com status inicial controlado.
4. **Consulta e atualização de ordens** — Busca por ID, listagem e atualização de informações técnicas e financeiras preservando dados sensíveis do fluxo.
5. **Fluxo de status** — Endpoint específico para status e regras explícitas que impedem saltos de etapas ou reabertura de ordens encerradas.
6. **Documentação e qualidade** — Swagger/OpenAPI configurado e testes de integração cobrindo o comportamento público da API.
7. **Exclusão segura de ordens** — Regra de negócio que permite remover somente ordens abertas ou canceladas, preservando ordens que já avançaram no atendimento.
8. **Paginação e ordenação** — Listagens preparadas para crescer, com metadados de navegação e ordenação configurável.
9. **Filtros de ordens** — Consulta combinável por cliente, status e período de abertura, com validação de intervalo de datas.
10. **Migrações versionadas** — Flyway assume a criação e evolução do schema, enquanto o Hibernate valida a compatibilidade das entidades.

## Próximas evoluções

- autenticação e autorização;
- histórico de atualizações da ordem;
- cadastro de técnicos e acompanhamento de custos/lucro.

## Autor

Projeto pessoal desenvolvido por Helder, com foco em evolução prática de backend, arquitetura REST e qualidade de código.
