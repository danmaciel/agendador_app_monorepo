# Agendador de Serviços

Projeto demonstrativo de backend para gerenciamento de **usuários, serviços e agendamentos**, com autenticação e regras de negócio organizadas por funcionalidades.

## Visão técnica

- **Stack:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA e Flyway.
- **Persistência:** H2 no perfil de desenvolvimento e PostgreSQL no perfil `prod`.
- **Segurança:** autenticação JWT e refresh token, com configurações de produção exigidas via variáveis de ambiente. O cadastro de usuários é público; perfis e agendamentos possuem regras de autorização por proprietário ou administrador, e operações administrativas exigem `ROLE_ADMIN`.
- **Organização:** `feature/*/application/usecase`, `domain` e `infrastructure/controller`.
- **Qualidade:** testes JUnit/MockMvc e integração com H2 e PostgreSQL 15 via Testcontainers; GitHub Actions executa Maven em jobs separados.

### Principais casos de uso

- Cadastro e administração de usuários e serviços.
- Criação, aprovação, atualização, rejeição e desativação administrativa de agendamentos.
- Sugestões de datas, histórico e consultas de agendamentos.
- Login, renovação de token e controle de acesso.

## Estrutura

```text
backend/
├── src/main/java/.../
│   ├── feature/
│   │   ├── agendamento/
│   │   ├── auth/
│   │   ├── servico/
│   │   └── usuario/
│   └── config/
├── src/main/resources/
│   └── db/migration/      # Migrações Flyway
└── src/test/               # Testes automatizados
```

## Executar testes

Requer **JDK 21** e Maven.

```bash
cd backend
mvn verify
```

A pipeline [Backend CI](.github/workflows/ci.yml) executa `mvn -B verify` e, em outro job com Docker, `mvn -B -Ppostgres-it verify`, incluindo testes de concorrência reais com PostgreSQL 15. Em ambiente local com Docker disponível, você também pode executar `mvn -Ppostgres-it verify`.

## Execução local

O perfil padrão configura H2 em memória para desenvolvimento:

```bash
cd backend
mvn spring-boot:run
```

Para PostgreSQL, configure as variáveis de ambiente exigidas em `backend/src/main/resources/application-prod.properties` antes de iniciar com `SPRING_PROFILES_ACTIVE=prod`. O `docker-compose.yml` inicia apenas o banco; a API está comentada no Compose original.

Leia [SECURITY_SETUP.md](SECURITY_SETUP.md) e use [`.env.example`](.env.example) como referência. **Não publique credenciais reais.**

## Concorrência da agenda compartilhada

A criação normal, atualização, aprovação, rejeição e o aceite de sugestões de agendamentos adquirem um bloqueio transacional no banco (`SELECT ... FOR UPDATE` sobre uma única linha criada na migração Flyway V8) antes de consultar conflitos de horário. A linha permanece bloqueada até a transação concluir, serializando essas operações entre instâncias da aplicação que compartilhem o mesmo banco.

Essa é uma **estratégia conservadora de agenda única**: operações independentes, inclusive de datas diferentes, também aguardam a mesma trava. Ela prioriza a consistência do exemplo em detrimento da vazão. A criação **forçada**, exclusiva de administrador, mantém o comportamento intencional de ignorar conflitos e não usa essa trava; portanto ainda pode criar sobreposições.

O teste `AgendaConcurrencyPostgresIT` cobre disputas entre duas transações PostgreSQL: criação concorrente no mesmo horário, aprovação concorrente de reservas pendentes sobrepostas e atualização concorrente de reservas diferentes para o mesmo horário. Os testes exigem Docker/Testcontainers e rodam no perfil `postgres-it`. A estratégia não constitui prova de capacidade de produção, nem resolve requisitos futuros de múltiplos profissionais, fusos horários, horários que ultrapassam a meia-noite ou planejamento de recursos.

## Autorização e regras de domínio

O backend é um **projeto demonstrativo**, não um serviço publicado em produção. Para consultar e atualizar um agendamento por ID, o cliente autenticado deve ser seu proprietário ou administrador. Criação normal e aceite de sugestões também conferem o usuário do pedido; listagens individuais e histórico usam a mesma política de proprietário. Criação forçada, aprovação, rejeição, exclusão lógica e consultas administrativas são restritas ao papel de administrador.

O fluxo de estados das operações de aprovação e rejeição é intencionalmente restrito:

```text
PENDENTE --aprovar--> APROVADO
PENDENTE --rejeitar--> REJEITADO
```

Após aprovação ou rejeição, a rota de atualização de agendamento bloqueia mudanças de data, horário e serviços. O aceite de sugestões mantém o agendamento pendente da semana e acrescenta serviços **sem alterar sua data ou horário**; recalcula a duração e recusa sobreposição antes de salvar. A criação forçada, exclusiva para administradores, continua podendo ignorar conflitos.

Os testes de autorização MockMvc e testes unitários de transições complementam os testes concorrentes com PostgreSQL. São evidências de **cenários específicos**, não uma auditoria de segurança exaustiva nem uma garantia de comportamento sob toda condição de produção.

## Decisões e limitações

- Separação por funcionalidade e casos de uso ajuda a manter regras de negócio testáveis.
- Flyway mantém o esquema versionado; ambientes devem ser configurados explicitamente.
- O Docker Compose disponível não constitui uma implantação completa da aplicação.
- Os testes automatizados validam os cenários implementados, mas não substituem testes ponta a ponta e uma revisão de segurança de produção.
- Consultas de usuário e agendamento por ID possuem verificações de proprietário ou administrador; rotas pessoais e administrativas têm restrições específicas. Ainda é necessária revisão completa dos contratos, controles de acesso e APIs antes de qualquer exposição externa.
- Novos refresh tokens são segredos opacos de 256 bits, persistidos apenas como **fingerprint SHA-256**. Tokens legados em UUIDv4 ainda são aceitos temporariamente até sua rotação. A renovação usa bloqueio pessimista por usuário e token, com cenários de concorrência testados em PostgreSQL. **Ainda não há** família de tokens, detecção robusta de replay, política de múltiplos dispositivos ou garantia para todas as interleavings e falhas. Ver [plano de transição](docs/REFRESH_TOKEN_TRANSITION.md).
- O perfil padrão utiliza credenciais de administrador e JWT de desenvolvimento: execute apenas em ambiente descartável e isolado. Não publique a porta HTTP como serviço externo.
- Os endpoints de listagem, promoção a administrador e desativação de usuários são restritos a `ROLE_ADMIN`, com regressões MockMvc para usuários comuns e administradores. O cadastro continua público no laboratório.

## Escopo

Projeto de estudo/demonstração de engenharia de software. Não há declaração de disponibilidade ou implantação em produção.
