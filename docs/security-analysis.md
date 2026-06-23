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
