package com.lucas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.config.Keybind;

public final class KeybindDialog
{
    private KeybindDialog()
    {
    }

    public static Keybind captureKeybind(Component parent, String title)
    {
        AtomicReference<Keybind> result = new AtomicReference<>();
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JLabel label = new JLabel("Press a keybind. Esc clears.", SwingConstants.CENTER);
        label.setFocusable(true);
        label.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    result.set(Keybind.NOT_SET);
                    dialog.dispose();
                    return;
                }

                result.set(new Keybind(e));
                dialog.dispose();
            }
        });

        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                label.requestFocusInWindow();
            }
        });

        dialog.add(label, BorderLayout.CENTER);
        dialog.setSize(260, 90);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);

        return result.get();
    }
}
