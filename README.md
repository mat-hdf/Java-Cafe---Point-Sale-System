# ☕ Java Café - Point of Sale (POS) System

[![Java Version](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![GUI Framework](https://img.shields.io/badge/UI-Java%20Swing-blue?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Course](https://img.shields.io/badge/Curso-SCC0204%20POO-red?style=for-the-badge)](https://usp.br/)

Um sistema simplificado de Ponto de Venda (PDV) desenvolvido em **Java** com interface gráfica **Swing**. O sistema gerencia o lançamento de pedidos, controle dinâmico de inventário em tempo real, geração de relatórios gráficos de vendas e persistência de dados local.

Desenvolvido como requisito parcial para aprovação na disciplina **SCC0204 - Programação Orientada a Objetos** no primeiro semestre de 2026 (USP).

---

## 🚀 Funcionalidades Principais

### 1. Sistema de Pedidos (`Order Entry`)
* **Interface Intuitiva:** Seleção rápida de itens do cardápio (Café, Torta, Cappuccino, etc.) com botões visuais ilustrados.
* **Cálculo em Tempo Real:** Atualização automática de subtotal, taxa (imposto de 10%) e valor total no menu lateral à medida que os itens são adicionados ou removidos.
* **Emissão de Recibo:** Geração de cupom fiscal formatado em texto ao finalizar a venda, com suporte para salvar localmente como arquivo `.txt` ou imprimir.

### 2. Controle de Inventário (`Inventory`)
* **Monitoramento Dinâmico:** Visualização do inventário em tabela (`JTable`) contendo nome, preço e quantidade em estoque.
* **Atualização em Tempo Real:** O estoque dos produtos é decrementado automaticamente no momento em que uma venda é finalizada.
* **Edição Flexível:** Permite que funcionários editem os valores diretamente na tabela (com validações de formato) ou adicionem novos produtos via formulário inferior.
* **Controle Crítico de Estoque:** Alertas visuais automáticos caso o nível de estoque fique abaixo de um limite configurável pelo usuário.

### 3. Painel de Vendas (`Sales Dashboard`)
* **Dashboard Modernizado:** Visualização de indicadores de performance financeira (KPIs) segmentados por períodos: **Hoje**, **Semana Atual** e **Mês Atual**.
* **Métricas Apresentadas:**
  * 💵 Receita total calculada.
  * 🧾 Quantidade de transações concluídas.
  * 🏆 Top 3 produtos mais vendidos (com badges visuais dinâmicos `#1`, `#2` e `#3`).
* **Exportação de Relatórios:** Opção para exportar os dados do dashboard em formatos de texto formatado (`.txt`) ou planilha (`.csv`).

### 4. Persistência de Dados e Tratamento de Exceções
* **Armazenamento Local:** Todas as transações financeiras são salvas em `sales.csv` e o estoque atualizado é mantido em `inventory.csv`. Os dados são recarregados automaticamente ao abrir a aplicação.
* **Exceções Customizadas:** Lançamento de `OutOfStockException` para impedir vendas de itens indisponíveis no estoque, garantindo consistência e gerando avisos visuais amigáveis.

---

## 📂 Estrutura do Projeto

O projeto adota o padrão de arquitetura **MVC (Model-View-Controller)** para separar a lógica de negócios das interfaces gráficas:

```
├── Main.java                      # Ponto de entrada do sistema
├── CafeTheme.java                 # Gerenciador central de temas, cores e fontes customizadas
├── OrderGUI.java                  # Painel visual do carrinho e pedidos
├── OrderLogic.java                # Controlador lógico dos pedidos
├── InventoryGUI.java              # Painel visual da tabela de estoque
├── InventoryLogic.java            # Controlador lógico e persistência do inventário
├── SalesReportGUI.java            # Dashboard de métricas e design customizado
├── SalesPersistence.java          # Persistência de transações de vendas (CSV)
├── OutOfStockException.java       # Exceção customizada para falta de estoque
├── TestRunner.java                # Suíte de testes unitários integrada (Zero-dependency)
├── sales.csv                      # Banco de dados de vendas (Gerado no runtime)
└── inventory.csv                  # Banco de dados de estoque (Gerado no runtime)
```

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos
* **Java Development Kit (JDK) 17** ou superior instalado e configurado no PATH do sistema.

### Passo 1: Clonar o Repositório
```bash
git clone https://github.com/mat-hdf/Java-Cafe---Point-Sale-System.git
cd Java-Cafe---Point-Sale-System
```

### Passo 2: Compilar os Arquivos Java
Compile todas as classes do projeto utilizando o compilador padrão:
```bash
javac *.java
```

### Passo 3: Executar a Aplicação
Inicie a aplicação principal Java Cafe:
```bash
java Main
```

---

## 🧪 Testes Unitários

O projeto possui um executor de testes integrado e independente (`TestRunner.java`) para validar a integridade dos cálculos, persistência e manipulação lógica de dados:

Para rodar os testes unitários automáticos:
```bash
java TestRunner
```

### O que é testado:
1. **SalesPersistence Save and Load:** Gravação de itens vendidos e cálculo do total da transação.
2. **SalesReportGUI Filtering and Metrics:** Filtragem por períodos (hoje, semana, mês) e cálculo das métricas exibidas.
3. **OrderLogic Item Management:** Adição, cálculo de total com imposto (10%) e cancelamento de pedidos.
4. **InventoryLogic Operations:** Cadastro de novos produtos e modificação correta de linhas do estoque.

---

## 📝 Princípios de POO Aplicados

* **Encapsulamento:** Todos os atributos críticos das classes de lógica e modelos (como `SaleItem` e `SaleTransaction`) são privados e expostos estritamente através de métodos acessores.
* **Tratamento de Exceções Customizado:** Uso de `OutOfStockException` estendendo a classe base `Exception` para tratar cenários excepcionais de ruptura de estoque.
* **Separação de Responsabilidades:** Classes de interface (`GUI`) e controladores de regras de negócio (`Logic`) são desvinculadas, comunicando-se através de interfaces e padrões de projeto limpos.
