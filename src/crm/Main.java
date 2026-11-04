package crm;

import crm.storage.CrmStorage;
import crm.ui.MainFrame;

import javax.swing.*;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            CrmStorage storage = CrmStorage.load();
            new MainFrame(storage).setVisible(true);
        });
    }
}
