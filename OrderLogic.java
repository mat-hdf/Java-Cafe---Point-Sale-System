import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderLogic implements ActionListener {
    
    private OrderGUI gui;
    private double currentTotal = 0.0;

    public OrderLogic(OrderGUI gui) {
        this.gui = gui;
    }

    // 2. Este método é obrigatório por causa do 'implements ActionListener'
    @Override
    public void actionPerformed(ActionEvent e) {
        // Pega o texto exato escrito no botão que foi clicado (ex: "Pie", "Cake")
        String botaoClicado = e.getActionCommand(); 

        // Descobre qual botão chamou a lógica e age de acordo
        switch (botaoClicado) {
            case "Pie":
                adicionarItem("Pie", 15.50);
                break;
            case "Cake":
                adicionarItem("Cake", 12.00);
                break;
            case "Coffee":
                adicionarItem("Coffee", 5.00);
                break;
            // ... adicione os cases para Tea, Water, Capuccino
            case "Remove Item":
                removerItem();
                break;
        }
    }

    // --- Métodos Auxiliares que fazem o trabalho de verdade ---

    private void adicionarItem(String nomeItem, double preco) {
        // Adiciona a linha na tabela da GUI
        gui.getTableModel().addRow(new Object[]{1, nomeItem, String.format("%.2f", preco)});
        
        // Atualiza a soma e o texto na tela
        currentTotal += preco;
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));
    }

    private void removerItem() {
        int linhaSelecionada = gui.getOrderTable().getSelectedRow();
        
        if (linhaSelecionada != -1) { 
            // Pega o valor, converte para número e subtrai
            String precoStr = (String) gui.getTableModel().getValueAt(linhaSelecionada, 2);
            double precoItem = Double.parseDouble(precoStr.replace(",", "."));
            
            currentTotal -= precoItem;
            gui.getOrderValueLabel().setText(String.format("%.2f", Math.max(0, currentTotal)));
            
            // Remove a linha da tabela
            gui.getTableModel().removeRow(linhaSelecionada);
        }
    }
}