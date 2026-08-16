# Análise de Vulnerabilidades

Este documento descreve o processo utilizado para análise de vulnerabilidades do projeto `oficina-mvp-backend`.

## Ferramenta utilizada

A análise estática foi realizada com a ferramenta **SpotBugs**, utilizando o plugin **Find Security Bugs** 
para identificação de possíveis vulnerabilidades em código Java/Spring Boot.

O SpotBugs foi utilizado para gerar o relatório técnico em formato XML, contendo os avisos encontrados no código-fonte 
analisado.

## Processo de geração dos relatórios

O processo seguido foi:

1. Execução do SpotBugs via Maven para análise estática do projeto.
2. Geração do relatório técnico em XML.
3. Abertura da interface gráfica do SpotBugs com o comando `spotbugs:gui`.
4. Exportação do relatório XML para HTML por meio da interface gráfica.
5. Uso de IA para apoiar a leitura, interpretação e resumo dos achados técnicos.
6. Geração de um relatório final consolidado em formatos HTML e PDF, com linguagem mais clara, resumo executivo, 
classificação dos principais riscos e recomendações de correção.

## Diretório de relatórios

Os relatórios finais da análise ficam armazenados em:

```text
docs/reports/
```

Arquivos gerados:

```text
relatorio-vulnerabilidades-oficina-mvp.pdf
relatorio-vulnerabilidades-oficina-mvp.html
```

## Relatórios brutos

As saídas brutas das ferramentas, como XML, JSON, SARIF ou logs temporários, não devem ser versionadas no repositório 
principal, pois podem conter informações internas do ambiente local, caminhos da máquina ou detalhes técnicos sensíveis.

## Comandos utilizados

Para gerar a análise com o SpotBugs:

```powershell
mvn "-DskipTests" "spotbugs:spotbugs"
```

Para abrir a interface gráfica do SpotBugs:

```powershell
mvn "-DskipTests" "spotbugs:gui"
```

A partir da interface gráfica, o relatório XML foi exportado para HTML.

## Observação sobre o relatório final

O relatório HTML gerado diretamente pelo SpotBugs foi utilizado como evidência técnica, mas seu formato é mais voltado 
para análise de desenvolvedores.

Por isso, foi gerado um relatório final consolidado com apoio de IA, em formato HTML e PDF, com o objetivo de facilitar 
a leitura, organizar os achados por criticidade e apresentar recomendações de correção de forma mais clara.

## Estrutura recomendada

```text
docs/
  security-analysis.md
  reports/
    relatorio-vulnerabilidades-oficina-mvp.pdf
    relatorio-vulnerabilidades-oficina-mvp.html
```


# Análise com SonarQube Local (Docker)

Como complemento à análise do SpotBugs, o **SonarQube** foi utilizado localmente via Docker para fornecer um dashboard visual com métricas de qualidade, cobertura de testes, duplicação de código, code smells e technical debt.

### Pré-requisitos

- **Docker Desktop** instalado e em execução no Windows.
- **Java 21** configurado (`JAVA_HOME` e `PATH`).
- Projeto compilável via Maven (`./mvnw clean compile`).

### Passo 1 — Subir o container do SonarQube

Com o Docker Desktop em execução (ícone verde na bandeja do sistema), abrir o terminal e rodar:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
```

Aguardar até que o container esteja saudável. Verificar o status com:

```bash
docker ps
```

O SonarQube pode levar de 1 a 2 minutos para iniciar completamente na primeira execução.

### Passo 2 — Acessar a interface web

Abrir no navegador:

```text
http://localhost:9000
```

Credenciais padrão na primeira execução:

```text
Usuário: admin
Senha: admin
```

No primeiro acesso, o sistema solicita a troca da senha. Definir uma nova senha e anotá-la.

### Passo 3 — Criar o projeto no SonarQube

1. Na interface web, clicar em **Projects → Create Project**.
2. Preencher os campos:
    - **Display name:** oficina-mvp-backend
    - **Project key:** oficina-mvp-backend
3. Clicar em **Create**.

### Passo 4 — Gerar um token de acesso

1. Clicar no avatar (canto superior direito) → **My Account → Security**.
2. Em **Generate Token**, definir um nome (ex: `local-analysis`) e clicar em **Generate**.
3. Copiar o token exibido — ele será utilizado no comando de análise.

> ⚠️ O token é exibido apenas uma vez. Copiá-lo antes de fechar a janela.

### Passo 5 — Executar a análise

No terminal, dentro da pasta do projeto, rodar:

```bash
./mvnw verify sonar:sonar \
  -Dsonar.projectKey=oficina-mvp-backend \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=TOKEN_GERADO_NO_PASSO_4
```

Substituir `TOKEN_GERADO_NO_PASSO_4` pelo token copiado anteriormente.


