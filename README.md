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

## Aprendizado com apoio de agentes de IA

O NexOS também é um projeto de aprendizado sobre desenvolvimento assistido por IA. O autor trabalha em colaboração com agentes para discutir modelagem, investigar alternativas, revisar código, criar testes e documentar decisões.

O escopo de cada etapa, as decisões de negócio e a aprovação das mudanças permanecem sob responsabilidade do autor. O histórico de commits, os testes automatizados e esta documentação tornam o processo de evolução verificável para quem analisar o projeto.

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
- Spring Security e JWT
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
| `PATCH` | `/service-orders/{id}/technician` | Atribui ou altera o técnico responsável |
| `PATCH` | `/service-orders/{id}/status` | Atualiza somente o status da ordem |
| `GET` | `/service-orders/{id}/status-history` | Consulta o histórico de mudanças de status |
| `DELETE` | `/service-orders/{id}` | Exclui uma ordem em situação permitida |

### Autenticação

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/auth/login` | Autentica um usuário e retorna um JWT |

### Usuários

Todas as rotas abaixo exigem um token de `ADMIN`.

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/users` | Cadastra um usuário, incluindo técnicos |
| `GET` | `/users` | Lista usuários de forma paginada |
| `GET` | `/users/{id}` | Busca um usuário por ID |
| `PUT` | `/users/{id}` | Atualiza nome, e-mail e papel do usuário |
| `PATCH` | `/users/{id}/password` | Redefine a senha de um usuário |
| `DELETE` | `/users/{id}` | Remove um usuário quando permitido |

Ao abrir uma ordem, o status inicial é `ABERTA` e a data de abertura é definida pelo servidor.

A ordem pode iniciar sem técnico responsável. Um `ADMIN` ou `ATENDENTE` atribui um usuário de papel `TECNICO` por meio do endpoint específico. A resposta da ordem inclui, quando houver atribuição, um resumo seguro do técnico com identificador, nome e e-mail. Ordens `CANCELADA` ou `FINALIZADA` não aceitam novas atribuições.

Um técnico pode consultar clientes e ordens, mas só pode alterar dados técnicos ou status de uma ordem atribuída ao próprio e-mail. A regra é verificada na camada de serviço, de modo que não dependa exclusivamente da proteção do endpoint. O `ADMIN` permanece autorizado a alterar qualquer ordem.

Quando uma ordem chega ao status `FINALIZADA`, a API registra automaticamente a `dataFinalizacao`. Para preservar a consistência financeira, `valor` e `custoReparo` devem estar preenchidos antes dessa transição.

O `PUT` não altera cliente, data de abertura nem status. Essas informações têm endpoints e regras próprias, evitando atualizações acidentais.

Uma ordem pode ser excluída somente enquanto estiver `ABERTA` ou depois de `CANCELADA`. Ordens em análise, reparo ou já finalizadas preservam seu histórico e retornam `409 Conflict` caso a exclusão seja solicitada.

## Paginação e ordenação

As listagens de clientes e ordens de serviço aceitam os parâmetros `page`, `size` e `sort`. A resposta inclui os registros em `content` e metadados como página atual, total de elementos e total de páginas.

```text
GET /clients?page=0&size=10&sort=nome,asc
GET /service-orders?page=0&size=10&sort=dataAbertura,desc
```

Sem parâmetros, a API retorna a primeira página com até 10 registros. Clientes e usuários são ordenados por `id`; ordens são ordenadas por data de abertura decrescente.

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

Cada transição válida é registrada com status anterior, novo status e data da alteração. O histórico pode ser consultado em `GET /service-orders/{id}/status-history` e é removido somente quando uma ordem que pode ser excluída é apagada junto com seus registros relacionados.

## Validações e respostas de erro

Os DTOs validam campos obrigatórios, tamanho de textos e valores monetários não negativos antes que a regra de negócio seja executada.

| Situação | Resposta |
| --- | --- |
| Corpo de requisição inválido | `400 Bad Request` |
| Cliente ou ordem não encontrada | `404 Not Found` |
| Transição de status inválida | `409 Conflict` |
| E-mail de usuário já cadastrado | `409 Conflict` |
| Exclusão ou alteração do último administrador | `409 Conflict` |
| Técnico inválido ou atribuição em ordem encerrada | `409 Conflict` |
| Técnico tenta alterar ordem de outro técnico ou sem responsável | `403 Forbidden` |
| Finalização sem valor ou custo de reparo | `409 Conflict` |

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
JWT_SECRET=uma-chave-secreta-com-no-minimo-32-caracteres
ADMIN_NAME=Administrador NexOS
ADMIN_EMAIL=admin@nexos.local
ADMIN_PASSWORD=uma-senha-inicial-segura
```

Depois, inicie a API usando o Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação será iniciada, por padrão, em `http://localhost:8080`.

As três variáveis `ADMIN_*` são usadas somente para criar o primeiro administrador, caso ele ainda não exista. Não as inclua no controle de versão.

## Segurança e acesso

A API é stateless e exige um token JWT para todas as rotas de clientes e ordens de serviço. O login é público:

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@nexos.local",
  "senha": "uma-senha-inicial-segura"
}
```

Use o token retornado em cada requisição protegida:

```http
Authorization: Bearer <token>
```

| Papel | Permissões |
| --- | --- |
| `ADMIN` | Acesso total, incluindo exclusões e atribuição de técnicos |
| `ATENDENTE` | Gerencia clientes, consulta informações, abre ordens e atribui técnicos |
| `TECNICO` | Consulta clientes e ordens; altera dados técnicos e status apenas das ordens atribuídas a ele |

As senhas são armazenadas com hash BCrypt e jamais aparecem nas respostas da API. E-mails de usuários são normalizados para letras minúsculas. O JWT tem validade configurável, atualmente de duas horas.

O `ADMIN` cadastra atendentes e técnicos em `POST /users`, informando `nome`, `email`, `senha` e `role`. A alteração de senha possui endpoint próprio para não misturá-la com atualizações cadastrais. Para impedir o bloqueio administrativo da aplicação, não é possível excluir a própria conta nem remover ou rebaixar o último `ADMIN`. Um técnico também não pode ser excluído enquanto possuir ordens de serviço atribuídas.

Exemplo de atribuição de técnico:

```http
PATCH /service-orders/42/technician
Authorization: Bearer <token-de-admin-ou-atendente>
Content-Type: application/json
```

```json
{
  "tecnicoId": 7
}
```

## Migrações de banco de dados

O schema é versionado com Flyway. A migração `V1__create_initial_schema.sql` cria as tabelas de clientes e ordens de serviço, a `V2__create_service_order_status_history.sql` adiciona o histórico de status, a `V3__create_users.sql` adiciona usuários e seus papéis, a `V4__add_technician_to_service_orders.sql` cria o vínculo opcional com o técnico responsável e a `V5__add_completion_date_to_service_orders.sql` adiciona a data de finalização. As migrações também criam índices usados nas consultas por cliente, status, datas e técnico.

O Hibernate utiliza `ddl-auto=validate`: ele confere se as entidades correspondem ao schema, mas não cria nem altera tabelas. Toda evolução estrutural deve ser adicionada como uma nova migração em `src/main/resources/db/migration`.

Para compatibilidade com um banco local já criado antes da adoção do Flyway, a aplicação usa `baseline-on-migrate=true`. Em ambientes com dados importantes, faça backup e revise a migração antes da primeira execução.

## Documentação interativa

Com a aplicação em execução, a documentação pode ser acessada em:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI em JSON: `http://localhost:8080/v3/api-docs`

O Swagger UI permite visualizar contratos, campos, respostas e executar requisições diretamente pelo navegador. Para testar rotas protegidas, use o botão **Authorize** e informe `Bearer <token>`.

## Testes

O projeto possui testes de integração para os endpoints de clientes, usuários, ordens de serviço e documentação OpenAPI. Os testes usam H2 em modo de compatibilidade com PostgreSQL, sem depender do banco local.

```powershell
.\mvnw.cmd clean test
```

Atualmente, a suíte possui 66 testes automatizados cobrindo cenários de sucesso, validação, recursos inexistentes, regras de exclusão, paginação, ordenação, filtros, migração de banco, histórico, autenticação JWT, autorização por papel, gestão de usuários, atribuição de técnicos, isolamento de ordens por técnico, data de finalização e transições de status inválidas.

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
11. **Histórico de status** — Cada transição válida é auditada com os estados anterior e novo, além do instante da alteração.
12. **Autenticação e autorização** — Login JWT, senhas protegidas com BCrypt e permissões definidas por papel de usuário.
13. **Gestão administrativa de usuários** — Cadastro, consulta, atualização, redefinição de senha e remoção segura de usuários, incluindo técnicos.
14. **Técnicos nas ordens de serviço** — Vínculo opcional, atribuição por endpoint próprio, validação do papel do usuário e proteção contra exclusão de técnico em uso.
15. **Isolamento de alterações técnicas** — Técnicos só alteram dados e status das ordens atribuídas a eles; administradores mantêm acesso total.
16. **Finalização financeira consistente** — A conclusão registra data automaticamente e exige valor e custo de reparo preenchidos.

## Próximas evoluções

- acompanhamento de custos, faturamento e lucro de ordens finalizadas.
- renovação e revogação de tokens;
- desativação de usuários sem apagar seu histórico.

## Autor

Projeto pessoal desenvolvido por Helder, com foco em evolução prática de backend, arquitetura REST, qualidade de código e desenvolvimento responsável com apoio de agentes de IA.
