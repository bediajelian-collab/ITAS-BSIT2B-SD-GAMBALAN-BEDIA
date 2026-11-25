
package com.mycompany.campuslostandfound;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.mycompany.campuslostandfound.gui.ClientView;

public class CampusLostAndFound {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) { }
            new ClientView().setVisible(true);
        });
    }
}