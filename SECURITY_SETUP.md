# Configuracao segura (desenvolvimento e producao)

- Configuracoes de producao requerem `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_LOGIN` e `APP_ADMIN_SENHA` no ambiente.
- Para Docker Compose, defina `POSTGRES_PASSWORD` localmente (por exemplo em um `.env` nao versionado). Copie `.env.example` e preencha com valores fortes antes de iniciar.
- O perfil padrao `application.properties` ainda contem valores de desenvolvimento para H2 e administrador: nunca o exponha como ambiente de producao.
- A API no Compose permanece comentada como no projeto original; essa mudanca nao a ativa.
- Qualquer senha, conta inicial ou segredo JWT anteriormente publicado que tenha sido utilizado deve ser rotacionado ou revogado. Remover valores da branch atual nao apaga o historico Git.
- Antes de mesclar, verifique inicializacao com `SPRING_PROFILES_ACTIVE=prod` e as variaveis exigidas, sem imprimir segredos nos logs.

## Limites de autenticacao ainda abertos

- A regra de autorizacao HTTP permite **somente** `POST /api/v1/usuario` sem autenticacao para cadastro. A consulta `GET /api/v1/usuario/{id}` exige autenticacao e autoriza apenas o proprio usuario ou administradores, antes de buscar o ID solicitado; listagem, promocao e remocao possuem regras administrativas. Nao generalize essa politica para outros recursos sem testes especificos.
- Novos refresh tokens sao armazenados apenas como fingerprint SHA-256. A compatibilidade transitoria com tokens UUIDv4 legados mantem registros antigos em texto ate rotacao ou expiracao; ver [plano de transicao](docs/REFRESH_TOKEN_TRANSITION.md). A rotacao ainda nao implementa familias, replay robusto ou consumo unico concorrente. Nao anunciar como sessao pronta para producao.
- O perfil de desenvolvimento possui login/senha inicial conhecidos. Nunca utilizar esse perfil em servidor acessivel externamente. O perfil `prod` exige credenciais fornecidas fora do codigo.
- Antes de ativar qualquer uso real, adicionar testes de autorizacao por usuario, revisar gestao de sessao, rate limiting e tratamento de erros. Valores antigos publicados precisam ser rotacionados se tiverem sido usados fora de laboratorio.

- O caso de uso de refresh agora recusa explicitamente a emissao de novos access JWTs quando a conta vinculada esta desativada. Isto nao revoga access JWTs previamente emitidos nem substitui verificacao atomica de estado da conta com a rotacao; conferir essa corrida numa futura revisao do armazenamento.
